package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.service.EmployeeWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeWorkflowService employeeWorkflowService;

    @GetMapping("/employee")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeDashboard(Authentication authentication) {
        Map<String, Object> data = employeeWorkflowService.getEmployeeDashboard(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(data, "Employee dashboard retrieved successfully"));
    }
}
