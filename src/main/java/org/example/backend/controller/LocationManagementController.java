package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.LocationCreateRequest;
import org.example.backend.dto.request.UpdateLocationRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Location;
import org.example.backend.entity.Warehouse;
import org.example.backend.enums.LocationType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.example.backend.repository.StockLedgerRepository;
import org.example.backend.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/locations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class LocationManagementController {

    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockLedgerRepository stockLedgerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllLocations(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) LocationType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Location> locations = locationRepository.findWithFilters(warehouseId, zone, type, active,
                PageRequest.of(page, size));

        List<Map<String, Object>> content = locations.getContent().stream()
                .map(this::toLocationMap).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", locations.getTotalElements());
        response.put("totalPages", locations.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Locations retrieved successfully"));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLocationById(@PathVariable UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));

        Map<String, Object> response = toLocationMap(location);
        // Add current stock info
        List<UUID> productIds = stockLedgerRepository.findDistinctProductIdsByLocationId(locationId);
        response.put("currentStockProductCount", productIds.size());

        return ResponseEntity.ok(ApiResponse.success(response, "Location retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createLocation(
            @Valid @RequestBody LocationCreateRequest request) {
        if (locationRepository.existsByCode(request.getCode())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location code already exists: " + request.getCode()));
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.getWarehouseId()));

        Location location = Location.builder()
                .code(request.getCode())
                .warehouse(warehouse)
                .zone(request.getZone())
                .type(request.getType())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .positionZ(request.getPositionZ() != null ? request.getPositionZ() : 0.0)
                .build();

        location = locationRepository.save(location);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toLocationMap(location), "Location created successfully"));
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody UpdateLocationRequest request) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));

        if (request.getZone() != null)
            location.setZone(request.getZone());
        if (request.getType() != null)
            location.setType(request.getType());
        if (request.getPositionX() != null)
            location.setPositionX(request.getPositionX());
        if (request.getPositionY() != null)
            location.setPositionY(request.getPositionY());
        if (request.getActive() != null)
            location.setActive(request.getActive());

        location = locationRepository.save(location);

        return ResponseEntity.ok(ApiResponse.success(toLocationMap(location), "Location updated successfully"));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteLocation(@PathVariable UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));

        location.setActive(false);
        locationRepository.save(location);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Location deleted successfully"), "Location deleted successfully"));
    }

    private Map<String, Object> toLocationMap(Location location) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", location.getId());
        map.put("code", location.getCode());
        map.put("warehouseCode", location.getWarehouse().getCode());
        map.put("warehouseId", location.getWarehouse().getId());
        map.put("zone", location.getZone());
        map.put("type", location.getType().name());
        map.put("positionX", location.getPositionX());
        map.put("positionY", location.getPositionY());
        map.put("positionZ", location.getPositionZ());
        map.put("distanceFromReceipt", location.getDistanceFromReceipt());
        map.put("distanceFromExpedition", location.getDistanceFromExpedition());
        map.put("active", location.getActive());
        map.put("createdAt", location.getCreatedAt());
        map.put("updatedAt", location.getUpdatedAt());
        return map;
    }
}
