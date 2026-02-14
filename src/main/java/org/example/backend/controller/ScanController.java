package org.example.backend.controller;

import jakarta.validation.constraints.NotBlank;
import org.example.backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/scan")
public class ScanController {

    @PostMapping("/decode")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> decode(@RequestBody ScanRequest request) {
        String barcodeData = request.barcodeData == null ? "" : request.barcodeData.trim();

        String entityType;
        if (barcodeData.startsWith("CH-")) {
            entityType = "CHARIOT";
        } else if (barcodeData.contains("-") && barcodeData.startsWith("B")) {
            entityType = "LOCATION";
        } else {
            entityType = "PRODUCT";
        }

        Map<String, Object> response = Map.of(
                "entityType", entityType,
                "entityDetails", Map.of("code", barcodeData, "context", request.scanContext),
                "suggestedAction", "Proceed with current task validation");

        return ResponseEntity.ok(ApiResponse.success(response, "Barcode decoded successfully"));
    }

    private record ScanRequest(@NotBlank String barcodeData, String scanContext) {
    }
}