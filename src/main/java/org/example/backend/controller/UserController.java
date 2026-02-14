package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(user), "User retrieved"));
    }

    @GetMapping("/{id}/current-task")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentTask(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        Transaction currentTask = transactionRepository.findByAssignedTo_Id(id).stream()
                .filter(t -> t.getStatus() == TransactionStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        if (currentTask != null) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", currentTask.getId());
            taskMap.put("type", currentTask.getType());
            taskMap.put("reference", currentTask.getReference());
            taskMap.put("status", currentTask.getStatus());
            taskMap.put("startedAt", currentTask.getStartedAt());
            result.put("currentTask", taskMap);
        } else {
            result.put("currentTask", null);
        }

        return ResponseEntity.ok(ApiResponse.success(result, "Current task retrieved"));
    }

    private Map<String, Object> toMap(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("username", u.getUsername());
        map.put("email", u.getEmail());
        map.put("firstName", u.getFirstName());
        map.put("lastName", u.getLastName());
        map.put("fullName", u.getFullName());
        map.put("role", u.getRole());
        map.put("active", u.getActive());
        return map;
    }
}
