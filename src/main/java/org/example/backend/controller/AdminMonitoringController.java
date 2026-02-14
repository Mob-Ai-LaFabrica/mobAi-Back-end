package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Chariot;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.ChariotStatus;
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

import java.util.*;

@RestController
@RequestMapping("/admin/monitoring")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ChariotRepository chariotRepository;

    @GetMapping("/warehouse-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWarehouseStatus() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Employees with positions and current tasks
        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        List<Map<String, Object>> employeesList = employees.stream().map(emp -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", emp.getId());
            map.put("fullName", emp.getFullName());
            map.put("username", emp.getUsername());

            // Current task
            List<Transaction> activeTasks = transactionRepository.findByAssignedTo_Id(emp.getId()).stream()
                    .filter(t -> t.getStatus() == TransactionStatus.IN_PROGRESS)
                    .toList();
            map.put("currentTask", activeTasks.isEmpty() ? null
                    : Map.of(
                            "id", activeTasks.get(0).getId(),
                            "reference", activeTasks.get(0).getReference(),
                            "type", activeTasks.get(0).getType().name()));

            map.put("currentPositionX", emp.getCurrentPositionX());
            map.put("currentPositionY", emp.getCurrentPositionY());
            map.put("lastPositionUpdate", emp.getLastPositionUpdate());
            map.put("status", activeTasks.isEmpty() ? "IDLE" : "WORKING");
            return map;
        }).toList();
        response.put("employees", employeesList);

        // Chariots
        List<Chariot> chariots = chariotRepository.findAll();
        List<Map<String, Object>> chariotList = chariots.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("chariotId", c.getId());
            map.put("code", c.getCode());
            map.put("status", c.getStatus().name());
            map.put("currentLocation", c.getCurrentLocation() != null ? Map.of(
                    "id", c.getCurrentLocation().getId(),
                    "code", c.getCurrentLocation().getCode()) : null);
            map.put("assignedTo", c.getLastUsedBy() != null &&
                    c.getStatus() == ChariotStatus.IN_USE
                            ? Map.of(
                                    "id", c.getLastUsedBy().getId(),
                                    "username", c.getLastUsedBy().getUsername())
                            : null);
            return map;
        }).toList();
        response.put("chariots", chariotList);

        // Active operations
        List<Transaction> activeOps = transactionRepository.findByStatus(TransactionStatus.IN_PROGRESS);
        List<Map<String, Object>> operationsList = activeOps.stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("transactionId", t.getId());
            map.put("reference", t.getReference());
            map.put("type", t.getType().name());
            map.put("assignedTo", t.getAssignedTo() != null ? Map.of(
                    "id", t.getAssignedTo().getId(),
                    "username", t.getAssignedTo().getUsername(),
                    "fullName", t.getAssignedTo().getFullName()) : null);
            int totalLines = t.getTransactionLines() != null ? t.getTransactionLines().size() : 0;
            map.put("progress", totalLines > 0 ? "0/" + totalLines + " lines completed" : "No lines");
            return map;
        }).toList();
        response.put("activeOperations", operationsList);

        return ResponseEntity.ok(ApiResponse.success(response, "Warehouse status retrieved"));
    }
}
