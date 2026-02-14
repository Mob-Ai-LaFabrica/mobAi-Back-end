package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.StockLedger;
import org.example.backend.repository.StockLedgerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockLedgerRepository stockLedgerRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockSummary() {
        List<StockLedger> all = stockLedgerRepository.findAll();
        long totalEntries = all.size();
        long totalProducts = all.stream().map(s -> s.getProduct().getId()).distinct().count();
        long totalLocations = all.stream().map(s -> s.getLocation().getId()).distinct().count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEntries", totalEntries);
        summary.put("totalProducts", totalProducts);
        summary.put("totalLocations", totalLocations);

        return ResponseEntity.ok(ApiResponse.success(summary, "Stock summary retrieved"));
    }

    @GetMapping("/by-product/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockByProduct(@PathVariable UUID id) {
        List<Map<String, Object>> result = stockLedgerRepository.findByProduct_Id(id).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Stock by product retrieved"));
    }

    @GetMapping("/by-location/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockByLocation(@PathVariable UUID id) {
        List<Map<String, Object>> result = stockLedgerRepository.findByLocation_Id(id).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Stock by location retrieved"));
    }

    @GetMapping("/ledger")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockLedger() {
        List<Map<String, Object>> result = stockLedgerRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Stock ledger retrieved"));
    }

    private Map<String, Object> toMap(StockLedger s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("movementType", s.getMovementType());
        map.put("quantity", s.getQuantity());
        map.put("runningBalance", s.getRunningBalance());
        map.put("performedAt", s.getPerformedAt());
        if (s.getProduct() != null) {
            map.put("product", Map.of(
                    "id", s.getProduct().getId(),
                    "sku", s.getProduct().getSku(),
                    "name", s.getProduct().getName()));
        }
        if (s.getLocation() != null) {
            map.put("location", Map.of(
                    "id", s.getLocation().getId(),
                    "code", s.getLocation().getCode()));
        }
        if (s.getTransaction() != null) {
            map.put("transactionId", s.getTransaction().getId());
        }
        return map;
    }
}
