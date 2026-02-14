package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.CreateChariotRequest;
import org.example.backend.dto.request.UpdateChariotRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Chariot;
import org.example.backend.enums.ChariotStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ChariotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/chariots")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ChariotManagementController {

    private final ChariotRepository chariotRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllChariots(
            @RequestParam(required = false) ChariotStatus status) {

        List<Chariot> chariots = status != null ? chariotRepository.findByStatus(status) : chariotRepository.findAll();

        List<Map<String, Object>> content = chariots.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("code", c.getCode());
            map.put("status", c.getStatus().name());
            map.put("currentLocation", c.getCurrentLocation() != null ? Map.of(
                    "id", c.getCurrentLocation().getId(),
                    "code", c.getCurrentLocation().getCode()) : null);
            map.put("lastUsedBy", c.getLastUsedBy() != null ? Map.of(
                    "id", c.getLastUsedBy().getId(),
                    "username", c.getLastUsedBy().getUsername(),
                    "fullName", c.getLastUsedBy().getFullName()) : null);
            map.put("lastUsedAt", c.getLastUsedAt());
            map.put("active", c.getActive());
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(content, "Chariots retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createChariot(
            @Valid @RequestBody CreateChariotRequest request) {
        if (chariotRepository.existsByCode(request.getCode())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chariot code already exists: " + request.getCode()));
        }

        Chariot chariot = Chariot.builder()
                .code(request.getCode())
                .build();

        chariot = chariotRepository.save(chariot);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", chariot.getId());
        response.put("code", chariot.getCode());
        response.put("status", chariot.getStatus().name());
        response.put("active", chariot.getActive());
        response.put("createdAt", chariot.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Chariot created successfully"));
    }

    @PutMapping("/{chariotId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateChariot(
            @PathVariable UUID chariotId,
            @Valid @RequestBody UpdateChariotRequest request) {

        Chariot chariot = chariotRepository.findById(chariotId)
                .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + chariotId));

        if (request.getStatus() != null)
            chariot.setStatus(request.getStatus());
        if (request.getActive() != null)
            chariot.setActive(request.getActive());

        chariot = chariotRepository.save(chariot);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", chariot.getId());
        response.put("code", chariot.getCode());
        response.put("status", chariot.getStatus().name());
        response.put("active", chariot.getActive());
        response.put("updatedAt", chariot.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "Chariot updated successfully"));
    }

    @DeleteMapping("/{chariotId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteChariot(@PathVariable UUID chariotId) {
        Chariot chariot = chariotRepository.findById(chariotId)
                .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + chariotId));

        chariot.setActive(false);
        chariotRepository.save(chariot);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Chariot deleted successfully"), "Chariot deleted successfully"));
    }
}
