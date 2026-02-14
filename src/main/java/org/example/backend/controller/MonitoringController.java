package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Chariot;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.Role;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.repository.ChariotRepository;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ChariotRepository chariotRepository;

    @GetMapping("/active-operations")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveOperations() {
        List<Map<String, Object>> ops = transactionRepository.findByStatus(TransactionStatus.IN_PROGRESS).stream()
                .map(this::toOperationMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(ops, "Active operations retrieved"));
    }

    @GetMapping("/employee-status")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeeStatus() {
        List<User> employees = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE && u.getActive())
                .collect(Collectors.toList());

        List<Transaction> activeTransactions = transactionRepository.findByStatus(TransactionStatus.IN_PROGRESS);

        List<Map<String, Object>> result = employees.stream().map(emp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", emp.getId());
            map.put("username", emp.getUsername());
            map.put("fullName", emp.getFullName());

            Transaction currentTask = activeTransactions.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(emp.getId()))
                    .findFirst()
                    .orElse(null);

            if (currentTask != null) {
                map.put("status", "BUSY");
                map.put("currentTask", Map.of(
                        "id", currentTask.getId(),
                        "type", currentTask.getType(),
                        "reference", currentTask.getReference()));
            } else {
                map.put("status", "AVAILABLE");
                map.put("currentTask", null);
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result, "Employee status retrieved"));
    }

    @GetMapping("/chariot-status")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChariotStatus() {
        List<Map<String, Object>> chariots = chariotRepository.findAll().stream()
                .map(this::toChariotMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(chariots, "Chariot status retrieved"));
    }

    private Map<String, Object> toOperationMap(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("type", t.getType());
        map.put("reference", t.getReference());
        map.put("status", t.getStatus());
        map.put("priority", t.getPriority());
        map.put("startedAt", t.getStartedAt());
        if (t.getAssignedTo() != null) {
            map.put("assignedTo", Map.of(
                    "id", t.getAssignedTo().getId(),
                    "username", t.getAssignedTo().getUsername(),
                    "fullName", t.getAssignedTo().getFullName()));
        }
        return map;
    }

    private Map<String, Object> toChariotMap(Chariot c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("code", c.getCode());
        map.put("status", c.getStatus());
        map.put("active", c.getActive());
        map.put("lastUsedAt", c.getLastUsedAt());
        if (c.getCurrentLocation() != null) {
            map.put("currentLocation", Map.of(
                    "id", c.getCurrentLocation().getId(),
                    "code", c.getCurrentLocation().getCode()));
        }
        if (c.getLastUsedBy() != null) {
            map.put("lastUsedBy", Map.of(
                    "id", c.getLastUsedBy().getId(),
                    "username", c.getLastUsedBy().getUsername()));
        }
        return map;
    }
}
