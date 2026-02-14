package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.AdminCreateTaskRequest;
import org.example.backend.dto.request.UpdateTaskRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Chariot;
import org.example.backend.entity.Product;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.enums.TransactionType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/tasks")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTaskController {

    private final TransactionRepository transactionRepository;
    private final TransactionLineRepository transactionLineRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final ChariotRepository chariotRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTasks(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) UUID assignedToId,
            @RequestParam(required = false) UUID createdById,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Transaction> tasks = transactionRepository.findWithFilters(
                type, status, assignedToId, createdById, PageRequest.of(page, size));

        List<Map<String, Object>> content = tasks.getContent().stream().map(this::toTaskMap).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", tasks.getTotalElements());
        response.put("totalPages", tasks.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTask(
            @Valid @RequestBody AdminCreateTaskRequest request,
            Authentication authentication) {

        User creator = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = Transaction.builder()
                .type(request.getType())
                .reference(generateReference(request.getType()))
                .status(TransactionStatus.PENDING)
                .priority(request.getPriority())
                .createdBy(creator)
                .notes(request.getNotes())
                .build();

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Assignee not found: " + request.getAssignedToId()));
            transaction.setAssignedTo(assignee);
            transaction.setAssignedAt(LocalDateTime.now());
        }

        if (request.getChariotId() != null) {
            Chariot chariot = chariotRepository.findById(request.getChariotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + request.getChariotId()));
            transaction.setChariot(chariot);
        }

        transaction = transactionRepository.save(transaction);

        // Create transaction lines
        if (request.getLines() != null) {
            int lineNumber = 1;
            for (AdminCreateTaskRequest.TaskLine line : request.getLines()) {
                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + line.getProductId()));

                TransactionLine tl = TransactionLine.builder()
                        .transaction(transaction)
                        .lineNumber(lineNumber++)
                        .product(product)
                        .quantity(line.getQuantity())
                        .build();

                if (line.getSourceLocationId() != null) {
                    tl.setSourceLocation(locationRepository.findById(line.getSourceLocationId())
                            .orElseThrow(() -> new ResourceNotFoundException("Source location not found")));
                }
                if (line.getDestinationLocationId() != null) {
                    tl.setDestinationLocation(locationRepository.findById(line.getDestinationLocationId())
                            .orElseThrow(() -> new ResourceNotFoundException("Destination location not found")));
                }

                transactionLineRepository.save(tl);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", transaction.getId());
        response.put("reference", transaction.getReference());
        response.put("type", transaction.getType().name());
        response.put("status", transaction.getStatus().name());
        response.put("createdAt", transaction.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Task created successfully"));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        Transaction transaction = transactionRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedToId()));
            transaction.setAssignedTo(assignee);
            transaction.setAssignedAt(LocalDateTime.now());
        }
        if (request.getPriority() != null)
            transaction.setPriority(request.getPriority());
        if (request.getStatus() != null) {
            transaction.setStatus(request.getStatus());
            if (request.getStatus() == TransactionStatus.COMPLETED) {
                transaction.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getNotes() != null)
            transaction.setNotes(request.getNotes());

        transaction = transactionRepository.save(transaction);

        return ResponseEntity.ok(ApiResponse.success(toTaskMap(transaction), "Task updated successfully"));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTask(@PathVariable UUID taskId) {
        Transaction transaction = transactionRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Task cancelled successfully"), "Task cancelled successfully"));
    }

    private Map<String, Object> toTaskMap(Transaction t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("reference", t.getReference());
        map.put("type", t.getType().name());
        map.put("status", t.getStatus().name());
        map.put("priority", t.getPriority().name());
        map.put("createdBy", Map.of("id", t.getCreatedBy().getId(), "username", t.getCreatedBy().getUsername()));
        map.put("assignedTo", t.getAssignedTo() != null ? Map.of(
                "id", t.getAssignedTo().getId(),
                "username", t.getAssignedTo().getUsername(),
                "fullName", t.getAssignedTo().getFullName()) : null);
        map.put("chariot", t.getChariot() != null ? Map.of(
                "id", t.getChariot().getId(), "code", t.getChariot().getCode()) : null);
        map.put("linesCount", t.getTransactionLines() != null ? t.getTransactionLines().size() : 0);
        map.put("notes", t.getNotes());
        map.put("createdAt", t.getCreatedAt());
        map.put("assignedAt", t.getAssignedAt());
        map.put("completedAt", t.getCompletedAt());
        return map;
    }

    private String generateReference(TransactionType type) {
        String prefix = switch (type) {
            case RECEIPT -> "RCV";
            case TRANSFER -> "TRF";
            case PICKING -> "PCK";
            case DELIVERY -> "DLV";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
