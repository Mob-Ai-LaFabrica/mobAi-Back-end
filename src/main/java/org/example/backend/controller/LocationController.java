package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Location;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('location:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllLocations() {
        List<Map<String, Object>> locations = locationRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(locations, "Locations retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('location:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLocation(@PathVariable UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(location), "Location retrieved"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('location:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchLocations(@RequestParam String query) {
        List<Map<String, Object>> results = locationRepository
                .findByCodeContainingIgnoreCaseOrZoneContainingIgnoreCase(query, query)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(results, "Locations matching '" + query + "'"));
    }

    private Map<String, Object> toMap(Location l) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", l.getId());
        map.put("code", l.getCode());
        map.put("zone", l.getZone());
        map.put("type", l.getType());
        map.put("active", l.getActive());
        return map;
    }
}
