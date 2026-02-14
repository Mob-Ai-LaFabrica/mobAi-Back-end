package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Warehouse;
import org.example.backend.repository.WarehouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Public/shared warehouse read endpoints (for all authenticated roles).
 * Admin CRUD is in WarehouseManagementController at /admin/warehouses.
 */
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('location:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll().stream()
                .filter(Warehouse::getActive).toList();

        List<Map<String, Object>> content = warehouses.stream().map(w -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("code", w.getCode());
            map.put("name", w.getName());
            map.put("city", w.getCity());
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(content, "Warehouses retrieved"));
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAuthority('location:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWarehouseById(@PathVariable UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new org.example.backend.exception.ResourceNotFoundException(
                        "Warehouse not found: " + warehouseId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", warehouse.getId());
        response.put("code", warehouse.getCode());
        response.put("name", warehouse.getName());
        response.put("city", warehouse.getCity());
        response.put("active", warehouse.getActive());

        return ResponseEntity.ok(ApiResponse.success(response, "Warehouse retrieved"));
    }
}
