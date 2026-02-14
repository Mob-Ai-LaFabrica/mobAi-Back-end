package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CreateWarehouseRequest;
import org.example.backend.dto.request.UpdateWarehouseRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Warehouse;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/warehouses")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class WarehouseManagementController {

    private final WarehouseRepository warehouseRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();

        List<Map<String, Object>> content = warehouses.stream().map(w -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("code", w.getCode());
            map.put("name", w.getName());
            map.put("city", w.getCity());
            map.put("active", w.getActive());
            map.put("locationCount", w.getLocations() != null ? w.getLocations().size() : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(content, "Warehouses retrieved successfully"));
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWarehouseById(@PathVariable UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", warehouse.getId());
        response.put("code", warehouse.getCode());
        response.put("name", warehouse.getName());
        response.put("city", warehouse.getCity());
        response.put("active", warehouse.getActive());
        response.put("locations", warehouse.getLocations() != null ? warehouse.getLocations().stream().map(l -> Map.of(
                "id", l.getId(),
                "code", l.getCode(),
                "zone", l.getZone() != null ? l.getZone() : "",
                "type", l.getType().name(),
                "active", l.getActive())).toList() : List.of());
        response.put("createdAt", warehouse.getCreatedAt());
        response.put("updatedAt", warehouse.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "Warehouse retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Warehouse code already exists: " + request.getCode()));
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode())
                .name(request.getName())
                .city(request.getCity())
                .build();

        warehouse = warehouseRepository.save(warehouse);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", warehouse.getId());
        response.put("code", warehouse.getCode());
        response.put("name", warehouse.getName());
        response.put("city", warehouse.getCity());
        response.put("active", warehouse.getActive());
        response.put("createdAt", warehouse.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Warehouse created successfully"));
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateWarehouse(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));

        if (request.getName() != null)
            warehouse.setName(request.getName());
        if (request.getCity() != null)
            warehouse.setCity(request.getCity());
        if (request.getActive() != null)
            warehouse.setActive(request.getActive());

        warehouse = warehouseRepository.save(warehouse);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", warehouse.getId());
        response.put("code", warehouse.getCode());
        response.put("name", warehouse.getName());
        response.put("city", warehouse.getCity());
        response.put("active", warehouse.getActive());
        response.put("updatedAt", warehouse.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "Warehouse updated successfully"));
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteWarehouse(@PathVariable UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));

        warehouse.setActive(false);
        warehouseRepository.save(warehouse);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Warehouse deleted successfully"), "Warehouse deleted successfully"));
    }
}
