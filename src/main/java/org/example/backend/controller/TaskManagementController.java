package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CreateTaskRequest;
import org.example.backend.dto.request.TaskAssignRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.Priority;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskManagementController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        User creator = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = Transaction.builder()
                .type(request.getType())
                .reference(request.getReference() != null ? request.getReference()
                        : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(TransactionStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .createdBy(creator)
                .notes(request.getNotes())
                .build();

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            transaction.setAssignedTo(assignee);
            transaction.setAssignedAt(LocalDateTime.now());
        }

        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toTaskMap(saved), "Task created successfully"));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskAssignRequest request) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User assignee = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        transaction.setAssignedTo(assignee);
        transaction.setAssignedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        return ResponseEntity.ok(ApiResponse.success(toTaskMap(saved), "Task assigned successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTasks(
            @RequestParam(required = false) String status) {

        List<Transaction> transactions;
        if (status != null && !status.isBlank()) {
            transactions = transactionRepository.findByStatus(TransactionStatus.valueOf(status.toUpperCase()));
        } else {
            transactions = transactionRepository.findAll();
        }

        List<Map<String, Object>> result = transactions.stream()
                .map(this::toTaskMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result, "All tasks retrieved successfully"));
    }

    @PutMapping("/{id}/reassign")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reassignTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskAssignRequest request) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User assignee = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        transaction.setAssignedTo(assignee);
        transaction.setAssignedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        return ResponseEntity.ok(ApiResponse.success(toTaskMap(saved), "Task reassigned successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        transactionRepository.delete(transaction);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    private Map<String, Object> toTaskMap(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("type", t.getType());
        map.put("reference", t.getReference());
        map.put("status", t.getStatus());
        map.put("priority", t.getPriority());
        map.put("createdAt", t.getCreatedAt());
        map.put("assignedAt", t.getAssignedAt());
        if (t.getAssignedTo() != null) {
            map.put("assignedTo", Map.of(
                    "id", t.getAssignedTo().getId(),
                    "username", t.getAssignedTo().getUsername(),
                    "fullName", t.getAssignedTo().getFullName()));
        } else {
            map.put("assignedTo", null);
        }
        map.put("notes", t.getNotes());
        return map;
    }
}
