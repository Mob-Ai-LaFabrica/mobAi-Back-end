package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.AuditLog;
import org.example.backend.entity.User;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.AuditLogRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLog> logs = auditLogRepository.findWithFilters(
                userId, action, entityType, entityId, startDate, endDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Map<String, Object>> content = logs.getContent().stream().map(this::toAuditLogMap).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserAuditHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<AuditLog> logs = auditLogRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName()));
        response.put("actions", logs.stream().map(this::toAuditLogMap).toList());

        return ResponseEntity.ok(ApiResponse.success(response, "User audit history retrieved"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEntityAuditHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {

        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("entityType", entityType);
        response.put("entityId", entityId);
        response.put("auditTrail", logs.stream().map(this::toAuditLogMap).toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Entity audit history retrieved"));
    }

    private Map<String, Object> toAuditLogMap(AuditLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("user", Map.of(
                "id", log.getUser().getId(),
                "username", log.getUser().getUsername(),
                "fullName", log.getUser().getFullName()));
        map.put("action", log.getAction());
        map.put("entityType", log.getEntityType());
        map.put("entityId", log.getEntityId());
        map.put("oldValue", log.getOldValue());
        map.put("newValue", log.getNewValue());
        map.put("ipAddress", log.getIpAddress());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
