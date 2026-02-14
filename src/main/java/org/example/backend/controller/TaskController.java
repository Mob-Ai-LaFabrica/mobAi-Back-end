package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.service.EmployeeWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final EmployeeWorkflowService employeeWorkflowService;

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyTasks(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
                employeeWorkflowService.getMyTasks(authentication.getName(), status),
                "My tasks retrieved successfully"));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskDetails(
            @PathVariable UUID taskId,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
                employeeWorkflowService.getTaskDetails(authentication.getName(), taskId),
                "Task details retrieved successfully"));
    }
}