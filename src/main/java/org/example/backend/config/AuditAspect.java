package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * AOP Aspect that automatically creates audit log entries for key operations.
 * Intercepts service methods to log:
 * - Operation lifecycle (start, execute line, complete)
 * - Stock movements (receipt, transfer, picking, delivery)
 * - AI decision overrides
 * - User management actions
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final UserRepository userRepository;

    // ===================== OPERATION LIFECYCLE =====================

    @AfterReturning(pointcut = "execution(* org.example.backend.service.EmployeeWorkflowService.startOperation(..))", returning = "result")
    public void auditStartOperation(JoinPoint joinPoint, Object result) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Map<String, Object> resultMap = (Map<String, Object>) result;
            UUID transactionId = (UUID) resultMap.get("transactionId");
            String type = (String) resultMap.get("type");

            auditService.log(user, "OPERATION_STARTED", "Transaction", transactionId,
                    null, "type=" + type);
        } catch (Exception e) {
            log.warn("Failed to create audit log for startOperation: {}", e.getMessage());
        }
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.service.EmployeeWorkflowService.executeOperationLine(..))", returning = "result")
    public void auditExecuteOperationLine(JoinPoint joinPoint, Object result) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Object[] args = joinPoint.getArgs();
            // args: username, ExecuteLineRequest
            Object request = args[1];
            // Use reflection-safe approach
            Map<String, Object> resultMap = (Map<String, Object>) result;

            auditService.log(user, "OPERATION_LINE_EXECUTED", "TransactionLine", null,
                    null, "lineNumber=" + resultMap.get("lineNumber") + ", status=" + resultMap.get("status"));
        } catch (Exception e) {
            log.warn("Failed to create audit log for executeOperationLine: {}", e.getMessage());
        }
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.service.EmployeeWorkflowService.completeOperation(..))", returning = "result")
    public void auditCompleteOperation(JoinPoint joinPoint, Object result) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Map<String, Object> resultMap = (Map<String, Object>) result;
            UUID transactionId = (UUID) resultMap.get("transactionId");
            String status = (String) resultMap.get("status");

            auditService.log(user, "OPERATION_COMPLETED", "Transaction", transactionId,
                    null, "status=" + status);
        } catch (Exception e) {
            log.warn("Failed to create audit log for completeOperation: {}", e.getMessage());
        }
    }

    // ===================== STOCK MOVEMENTS =====================

    @AfterReturning(pointcut = "execution(* org.example.backend.service.impl.StockLedgerServiceImpl.recordStockIn(..))", returning = "result")
    public void auditStockIn(JoinPoint joinPoint, Object result) {
        auditStockMovement(joinPoint, "STOCK_IN");
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.service.impl.StockLedgerServiceImpl.recordStockOut(..))", returning = "result")
    public void auditStockOut(JoinPoint joinPoint, Object result) {
        auditStockMovement(joinPoint, "STOCK_OUT");
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.service.impl.StockLedgerServiceImpl.recordAdjustment(..))", returning = "result")
    public void auditStockAdjustment(JoinPoint joinPoint, Object result) {
        auditStockMovement(joinPoint, "STOCK_ADJUSTMENT");
    }

    private void auditStockMovement(JoinPoint joinPoint, String action) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Object[] args = joinPoint.getArgs();
            // args: Product, Location, int quantity, Transaction, TransactionLine, User
            org.example.backend.entity.Product product = (org.example.backend.entity.Product) args[0];
            org.example.backend.entity.Location location = (org.example.backend.entity.Location) args[1];
            int quantity = (int) args[2];

            auditService.log(user, action, "StockLedger", product.getId(),
                    null, "product=" + product.getSku() + ", location=" + location.getCode() + ", qty=" + quantity);
        } catch (Exception e) {
            log.warn("Failed to create audit log for stock movement: {}", e.getMessage());
        }
    }

    // ===================== AI DECISION OVERRIDES =====================

    @AfterReturning(pointcut = "execution(* org.example.backend.controller.AiDecisionController.overrideDecision(..))")
    public void auditAiOverride(JoinPoint joinPoint) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Object[] args = joinPoint.getArgs();
            UUID decisionId = (UUID) args[0];

            auditService.log(user, "AI_DECISION_OVERRIDDEN", "AiDecisionLog", decisionId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for AI override: {}", e.getMessage());
        }
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.controller.AiDecisionController.approveDecision(..))")
    public void auditAiApproval(JoinPoint joinPoint) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Object[] args = joinPoint.getArgs();
            UUID decisionId = (UUID) args[0];

            auditService.log(user, "AI_DECISION_APPROVED", "AiDecisionLog", decisionId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for AI approval: {}", e.getMessage());
        }
    }

    @AfterReturning(pointcut = "execution(* org.example.backend.controller.AdminAiDecisionController.overrideAiDecision(..))")
    public void auditAdminAiOverride(JoinPoint joinPoint) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Object[] args = joinPoint.getArgs();
            UUID decisionId = (UUID) args[0];

            auditService.log(user, "ADMIN_AI_DECISION_OVERRIDDEN", "AiDecisionLog", decisionId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for admin AI override: {}", e.getMessage());
        }
    }

    // ===================== ISSUE REPORTING =====================

    @AfterReturning(pointcut = "execution(* org.example.backend.service.EmployeeWorkflowService.reportIssue(..))", returning = "result")
    public void auditReportIssue(JoinPoint joinPoint, Object result) {
        try {
            User user = getCurrentUser();
            if (user == null)
                return;

            Map<String, Object> resultMap = (Map<String, Object>) result;
            UUID discrepancyId = (UUID) resultMap.get("discrepancyId");

            auditService.log(user, "ISSUE_REPORTED", "TaskDiscrepancy", discrepancyId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for issue report: {}", e.getMessage());
        }
    }

    // ===================== HELPER =====================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
