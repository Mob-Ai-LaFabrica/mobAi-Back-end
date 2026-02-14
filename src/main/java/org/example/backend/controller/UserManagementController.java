package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CreateUserRequest;
import org.example.backend.dto.request.ResetPasswordRequest;
import org.example.backend.dto.request.UpdateUserRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.Role;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<User> users = userRepository.findWithFilters(role, active, PageRequest.of(page, size));

        List<Map<String, Object>> content = users.getContent().stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("role", u.getRole().name());
            map.put("active", u.getActive());
            map.put("currentPositionX", u.getCurrentPositionX());
            map.put("currentPositionY", u.getCurrentPositionY());
            map.put("lastPositionUpdate", u.getLastPositionUpdate());
            // Find current task
            List<Transaction> activeTasks = transactionRepository.findByAssignedTo_Id(u.getId()).stream()
                    .filter(t -> t.getStatus() == TransactionStatus.IN_PROGRESS
                            || t.getStatus() == TransactionStatus.PENDING)
                    .toList();
            map.put("currentTask", activeTasks.isEmpty() ? null
                    : Map.of(
                            "id", activeTasks.get(0).getId(),
                            "reference", activeTasks.get(0).getReference(),
                            "type", activeTasks.get(0).getType().name(),
                            "status", activeTasks.get(0).getStatus().name()));
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("role", user.getRole().name());
        response.put("active", user.getActive());
        response.put("currentPositionX", user.getCurrentPositionX());
        response.put("currentPositionY", user.getCurrentPositionY());
        response.put("lastPositionUpdate", user.getLastPositionUpdate());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "User retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username already exists"));
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole().name());
        response.put("active", user.getActive());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email already in use"));
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getActive() != null)
            user.setActive(request.getActive());

        user = userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("active", user.getActive());
        response.put("updatedAt", user.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setActive(false);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "User deleted successfully"), "User deleted successfully"));
    }

    @PutMapping("/{userId}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ResetPasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Password reset successfully"), "Password reset successfully"));
    }
}
