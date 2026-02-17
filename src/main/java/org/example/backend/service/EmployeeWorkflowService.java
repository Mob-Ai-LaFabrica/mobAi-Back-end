package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CompleteOperationRequest;
import org.example.backend.dto.request.ExecuteLineRequest;
import org.example.backend.dto.request.ReportIssueRequest;
import org.example.backend.dto.request.StartOperationRequest;
import org.example.backend.entity.Chariot;
import org.example.backend.entity.Product;
import org.example.backend.entity.ProductBarcode;
import org.example.backend.entity.TaskDiscrepancy;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.enums.IssueType;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ChariotRepository;
import org.example.backend.repository.ProductBarcodeRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.TaskDiscrepancyRepository;
import org.example.backend.repository.TransactionLineRepository;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeWorkflowService {

    private final TransactionRepository transactionRepository;
    private final TransactionLineRepository transactionLineRepository;
    private final ProductBarcodeRepository productBarcodeRepository;
    private final ProductRepository productRepository;
    private final TaskDiscrepancyRepository taskDiscrepancyRepository;
    private final UserRepository userRepository;
    private final ChariotRepository chariotRepository;
    private final ReceiptService receiptService;
    private final TransferService transferService;
    private final PickingService pickingService;
    private final DeliveryService deliveryService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyTasks(String username, String statusFilter) {
        List<Transaction> tasks;
        if (statusFilter == null || statusFilter.isBlank()) {
            tasks = transactionRepository.findByAssignedTo_Username(username);
        } else {
            TransactionStatus status = TransactionStatus.valueOf(statusFilter.trim().toUpperCase());
            tasks = transactionRepository.findByAssignedTo_UsernameAndStatus(username, status);
        }

        tasks.sort(Comparator.comparing(Transaction::getAssignedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());

        List<Map<String, Object>> response = new ArrayList<>();
        for (Transaction task : tasks) {
            response.add(toTaskCard(task));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTaskDetails(String username, UUID taskId) {
        Transaction task = getAssignedTransaction(username, taskId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", task.getId());
        payload.put("type", task.getType().name());
        payload.put("reference", task.getReference());
        payload.put("status", task.getStatus().name());

        List<Map<String, Object>> lines = new ArrayList<>();
        for (TransactionLine line : task.getTransactionLines()) {
            Map<String, Object> item = new HashMap<>();
            item.put("lineNumber", line.getLineNumber());
            item.put("product", toProductPayload(line.getProduct()));
            item.put("quantity", line.getQuantity());
            item.put("sourceLocation", toLocationPayload(line.getSourceLocation()));
            item.put("destinationLocation", toLocationPayload(line.getDestinationLocation()));
            lines.add(item);
        }
        payload.put("lines", lines);
        return payload;
    }

    public Map<String, Object> startOperation(String username, StartOperationRequest request) {
        Transaction task = getAssignedTransaction(username, request.getTransactionId());
        task.setStatus(TransactionStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());

        if (request.getChariotId() != null) {
            Chariot chariot = chariotRepository.findById(request.getChariotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + request.getChariotId()));
            task.setChariot(chariot);
        }

        transactionRepository.save(task);
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", task.getId());
        response.put("type", task.getType().name());
        response.put("status", task.getStatus());
        response.put("startedAt", task.getStartedAt());
        return response;
    }

    public Map<String, Object> executeOperationLine(String username, ExecuteLineRequest request) {
        Transaction task = getAssignedTransaction(username, request.getTransactionId());
        if (task.getStatus() != TransactionStatus.IN_PROGRESS) {
            throw new org.example.backend.exception.InvalidOperationException("Transaction must be IN_PROGRESS");
        }

        TransactionLine line = transactionLineRepository
                .findByTransaction_IdAndLineNumber(request.getTransactionId(), request.getLineNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction line not found"));

        validateScannedBarcode(line, request.getProductBarcode());

        // Process actual stock movement based on transaction type
        User performer = getUserByUsername(username);
        processStockMovement(task, line, performer);

        Map<String, Object> response = new HashMap<>();
        response.put("lineNumber", line.getLineNumber());
        response.put("status", "COMPLETED");
        response.put("product", toProductPayload(line.getProduct()));
        response.put("message", "Line processed and stock updated successfully");
        return response;
    }

    public Map<String, Object> reportIssue(String username, ReportIssueRequest request) {
        Transaction task = getAssignedTransaction(username, request.getTransactionId());
        User reporter = getUserByUsername(username);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        TransactionLine line = null;
        if (request.getLineNumber() != null) {
            line = transactionLineRepository.findByTransaction_IdAndLineNumber(task.getId(), request.getLineNumber())
                    .orElse(null);
        }

        TaskDiscrepancy discrepancy = TaskDiscrepancy.builder()
                .transaction(task)
                .transactionLine(line)
                .product(product)
                .issueType(IssueType.valueOf(request.getIssueType().trim().toUpperCase()))
                .expectedQuantity(request.getExpectedQuantity())
                .actualQuantity(request.getActualQuantity())
                .reportedBy(reporter)
                .notes(request.getNotes())
                .build();

        TaskDiscrepancy saved = taskDiscrepancyRepository.save(discrepancy);

        Map<String, Object> response = new HashMap<>();
        response.put("discrepancyId", saved.getId());
        response.put("reportedAt", saved.getReportedAt());
        return response;
    }

    public Map<String, Object> completeOperation(String username, UUID transactionId,
            CompleteOperationRequest request) {
        Transaction task = getAssignedTransaction(username, transactionId);
        String statusText = request.getStatus().trim().toUpperCase();
        if (!("COMPLETED".equals(statusText) || "FAILED".equals(statusText))) {
            throw new org.example.backend.exception.InvalidOperationException(
                    "Status must be COMPLETED or FAILED");
        }

        task.setStatus(TransactionStatus.valueOf(statusText));
        task.setCompletedAt(LocalDateTime.now());
        task.setNotes(request.getNotes());
        transactionRepository.save(task);

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", task.getId());
        response.put("status", task.getStatus().name());
        response.put("completedAt", task.getCompletedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeDashboard(String username) {
        List<Transaction> tasks = transactionRepository.findByAssignedTo_Username(username);
        long pending = tasks.stream().filter(t -> t.getStatus() == TransactionStatus.PENDING).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TransactionStatus.IN_PROGRESS).count();
        long completed = tasks.stream().filter(t -> t.getStatus() == TransactionStatus.COMPLETED).count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("tasksPending", pending);
        dashboard.put("tasksInProgress", inProgress);
        dashboard.put("tasksCompleted", completed);
        return dashboard;
    }

    /**
     * Dispatch stock movement to the appropriate operation service based on
     * transaction type.
     */
    private void processStockMovement(Transaction transaction, TransactionLine line, User performer) {
        switch (transaction.getType()) {
            case RECEIPT -> receiptService.processReceiptLine(transaction, line, performer);
            case TRANSFER -> transferService.processTransferLine(transaction, line, performer);
            case PICKING -> pickingService.processPickingLine(transaction, line, performer);
            case DELIVERY -> deliveryService.processDeliveryLine(transaction, line, performer);
            case ADJUSTMENT -> {
                // Adjustments are handled separately via admin endpoints
            }
        }
    }

    private void validateScannedBarcode(TransactionLine line, String scannedBarcode) {
        Product product = line.getProduct();
        if (product.getSku().equalsIgnoreCase(scannedBarcode)) {
            return;
        }

        ProductBarcode barcode = productBarcodeRepository.findByBarcode(scannedBarcode)
                .orElseThrow(
                        () -> new org.example.backend.exception.InvalidOperationException("Wrong product scanned"));

        if (!barcode.getProduct().getId().equals(product.getId())) {
            throw new org.example.backend.exception.InvalidOperationException("Wrong product scanned");
        }
    }

    private Transaction getAssignedTransaction(String username, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        if (transaction.getAssignedTo() == null || !username.equals(transaction.getAssignedTo().getUsername())) {
            throw new ResourceNotFoundException("Transaction not assigned to current employee: " + transactionId);
        }
        return transaction;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Map<String, Object> toTaskCard(Transaction task) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", task.getId());
        response.put("type", task.getType().name());
        response.put("reference", task.getReference());
        response.put("status", task.getStatus().name());
        response.put("priority", task.getPriority().name());
        response.put("assignedAt", task.getAssignedAt());
        return response;
    }

    private Map<String, Object> toProductPayload(Product product) {
        if (product == null) {
            return null;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", product.getId());
        payload.put("sku", product.getSku());
        payload.put("name", product.getName());
        payload.put("unitOfMeasure", product.getUnitOfMeasure());
        return payload;
    }

    private Map<String, Object> toLocationPayload(org.example.backend.entity.Location location) {
        if (location == null) {
            return null;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", location.getId());
        payload.put("code", location.getCode());
        payload.put("type", location.getType() != null ? location.getType().name() : null);
        return payload;
    }
}