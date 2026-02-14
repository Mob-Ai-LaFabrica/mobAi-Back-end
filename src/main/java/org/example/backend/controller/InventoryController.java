package org.example.backend.controller;

import org.example.backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/stock/product/{productId}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStock(@PathVariable String productId) {
        int totalStock = Math.abs(productId.hashCode() % 300) + 50;
        Map<String, Object> response = Map.of(
                "productId", productId,
                "productSku", "SKU-" + productId,
                "productName", "Product " + productId,
                "totalStock", totalStock,
                "unitOfMeasure", "pcs",
                "locations", List.of(
                        Map.of("locationCode", "B07-N1-A3", "quantity", totalStock / 2),
                        Map.of("locationCode", "B07-N2-C7", "quantity", totalStock - (totalStock / 2))));

        return ResponseEntity.ok(ApiResponse.success(response, "Product stock retrieved"));
    }

    @GetMapping("/stock/search")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchStockBySku(@RequestParam String sku) {
        int totalStock = Math.abs(sku.hashCode() % 250) + 30;
        Map<String, Object> response = Map.of(
                "productSku", sku,
                "productName", "Product for " + sku,
                "totalStock", totalStock,
                "unitOfMeasure", "pcs",
                "locations", List.of(
                        Map.of("locationCode", "B07-N1-A5", "quantity", totalStock / 2),
                        Map.of("locationCode", "B07-N2-B2", "quantity", totalStock - (totalStock / 2))));

        return ResponseEntity.ok(ApiResponse.success(response, "Stock search completed"));
    }

    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLocationContents(@PathVariable String locationId) {
        Map<String, Object> response = Map.of(
                "locationId", locationId,
                "locationCode", "LOC-" + locationId,
                "products", List.of(
                        Map.of("productSku", "MIXER-001", "quantity", 100),
                        Map.of("productSku", "VALVE-045", "quantity", 45)),
                "capacityStatus", "NORMAL");

        return ResponseEntity.ok(ApiResponse.success(response, "Location inventory retrieved"));
    }
}
