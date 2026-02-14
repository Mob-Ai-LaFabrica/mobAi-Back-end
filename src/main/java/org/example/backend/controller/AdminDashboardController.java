package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.AuditLog;
import org.example.backend.enums.ChariotStatus;
import org.example.backend.enums.Role;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ChariotRepository chariotRepository;
    private final TransactionRepository transactionRepository;
    private final TaskDiscrepancyRepository taskDiscrepancyRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboard() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Overview
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalProducts", productRepository.count());
        overview.put("totalLocations", locationRepository.count());
        overview.put("totalUsers", userRepository.count());
        overview.put("activeEmployees", userRepository.countByRoleAndActiveTrue(Role.EMPLOYEE));
        overview.put("activeChariots", chariotRepository.countByStatus(ChariotStatus.AVAILABLE)
                + chariotRepository.countByStatus(ChariotStatus.IN_USE));
        response.put("overview", overview);

        // Operations
        Map<String, Object> operations = new LinkedHashMap<>();
        operations.put("tasksInProgress", transactionRepository.countByStatus(TransactionStatus.IN_PROGRESS));
        operations.put("tasksPending", transactionRepository.countByStatus(TransactionStatus.PENDING));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        operations.put("tasksCompletedToday",
                transactionRepository.countByStatusAndCompletedAtAfter(TransactionStatus.COMPLETED, startOfDay));
        operations.put("unresolvedDiscrepancies", taskDiscrepancyRepository.countByResolvedAtIsNull());
        response.put("operations", operations);

        // AI Metrics
        Map<String, Object> aiMetrics = new LinkedHashMap<>();
        long totalAiDecisions = aiDecisionLogRepository.count();
        long overriddenDecisions = aiDecisionLogRepository.countByWasOverridden(true);
        aiMetrics.put("totalAiDecisions", totalAiDecisions);
        aiMetrics.put("overrideRate",
                totalAiDecisions > 0 ? Math.round((double) overriddenDecisions / totalAiDecisions * 10000.0) / 100.0
                        : 0.0);
        response.put("aiMetrics", aiMetrics);

        // Recent Activity
        List<AuditLog> recentLogs = auditLogRepository.findTop10ByOrderByCreatedAtDesc();
        response.put("recentActivity", recentLogs.stream().map(log -> {
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("id", log.getId());
            logMap.put("action", log.getAction());
            logMap.put("user", log.getUser().getUsername());
            logMap.put("entityType", log.getEntityType());
            logMap.put("createdAt", log.getCreatedAt());
            return logMap;
        }).toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Admin dashboard retrieved"));
    }
}
