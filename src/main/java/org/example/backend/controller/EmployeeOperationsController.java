package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CompleteOperationRequest;
import org.example.backend.dto.request.ExecuteLineRequest;
import org.example.backend.dto.request.ReportIssueRequest;
import org.example.backend.dto.request.StartOperationRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.service.EmployeeWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/operations")
@RequiredArgsConstructor
public class EmployeeOperationsController {

    private final EmployeeWorkflowService employeeWorkflowService;

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startOperation(
            @Valid @RequestBody StartOperationRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
                employeeWorkflowService.startOperation(authentication.getName(), request),
                "Operation started"));
    }

    @PostMapping("/execute-line")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeLine(
            @Valid @RequestBody ExecuteLineRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
                employeeWorkflowService.executeOperationLine(authentication.getName(), request),
                "Line executed successfully"));
    }

    @PostMapping("/report-issue")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reportIssue(
            @Valid @RequestBody ReportIssueRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        employeeWorkflowService.reportIssue(authentication.getName(), request),
                        "Issue reported"));
    }

    @PostMapping("/{transactionId}/complete")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeOperation(
            @PathVariable UUID transactionId,
            @Valid @RequestBody CompleteOperationRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
                employeeWorkflowService.completeOperation(authentication.getName(), transactionId, request),
                "Operation completed"));
    }
}