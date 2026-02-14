package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.StockLedger;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.MovementType;
import org.example.backend.enums.Role;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.repository.StockLedgerRepository;
import org.example.backend.repository.TaskDiscrepancyRepository;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReportsController {

    private final StockLedgerRepository stockLedgerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TaskDiscrepancyRepository taskDiscrepancyRepository;

    @GetMapping("/stock-movements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockMovementReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<StockLedger> movements = stockLedgerRepository.findMovementsByDateRange(productId, start, end);

        int totalIn = 0;
        int totalOut = 0;
        for (StockLedger ledger : movements) {
            if (ledger.getMovementType() == MovementType.IN) {
                totalIn += ledger.getQuantity();
            } else if (ledger.getMovementType() == MovementType.OUT) {
                totalOut += ledger.getQuantity();
            }
        }

        List<Map<String, Object>> movementList = movements.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("productSku", m.getProduct().getSku());
            map.put("productName", m.getProduct().getName());
            map.put("locationCode", m.getLocation().getCode());
            map.put("movementType", m.getMovementType().name());
            map.put("quantity", m.getQuantity());
            map.put("performedBy", m.getPerformedBy().getFullName());
            map.put("performedAt", m.getPerformedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", startDate + " to " + endDate);
        response.put("movements", movementList);
        response.put("summary", Map.of(
                "totalIn", totalIn,
                "totalOut", totalOut,
                "netChange", totalIn - totalOut));

        return ResponseEntity.ok(ApiResponse.success(response, "Stock movement report generated"));
    }

    @GetMapping("/user-productivity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProductivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID userId) {

        List<User> employees;
        if (userId != null) {
            employees = userRepository.findById(userId).map(List::of).orElse(List.of());
        } else {
            employees = userRepository.findByRole(Role.EMPLOYEE);
        }

        List<Map<String, Object>> userStats = employees.stream().map(user -> {
            List<Transaction> tasks = transactionRepository.findByAssignedTo_Id(user.getId());

            long completed = tasks.stream()
                    .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                    .filter(t -> t.getCompletedAt() != null &&
                            !t.getCompletedAt().toLocalDate().isBefore(startDate) &&
                            !t.getCompletedAt().toLocalDate().isAfter(endDate))
                    .count();

            long discrepancies = taskDiscrepancyRepository.findByResolvedAtIsNull().stream()
                    .filter(d -> d.getReportedBy() != null && d.getReportedBy().getId().equals(user.getId()))
                    .count();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", user.getId());
            map.put("fullName", user.getFullName());
            map.put("username", user.getUsername());
            map.put("tasksCompleted", completed);
            map.put("discrepanciesReported", discrepancies);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", startDate + " to " + endDate);
        response.put("users", userStats);

        return ResponseEntity.ok(ApiResponse.success(response, "User productivity report generated"));
    }
}
