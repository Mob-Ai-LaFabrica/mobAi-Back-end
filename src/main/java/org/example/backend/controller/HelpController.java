package org.example.backend.controller;

import org.example.backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/help")
public class HelpController {

    @GetMapping("/operations/{operationType}")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOperationHelp(@PathVariable String operationType) {
        Map<String, Object> response = Map.of(
                "operationType", operationType,
                "instructions", List.of(
                        "Confirm your assigned task",
                        "Scan source location and product",
                        "Validate quantity and destination",
                        "Submit completion once all lines are done"),
                "faq", List.of(
                        "If barcode fails, enter SKU manually.",
                        "Report discrepancies immediately to supervisor."));

        return ResponseEntity.ok(ApiResponse.success(response, "Operation help retrieved"));
    }
}