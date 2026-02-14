package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.ChariotAssignRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Chariot;
import org.example.backend.entity.User;
import org.example.backend.enums.ChariotStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ChariotRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chariots")
@RequiredArgsConstructor
public class ChariotController {

    private final ChariotRepository chariotRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllChariots() {
        List<Map<String, Object>> chariots = chariotRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(chariots, "Chariots retrieved"));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignChariot(
            @PathVariable UUID id,
            @Valid @RequestBody ChariotAssignRequest request) {

        Chariot chariot = chariotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + id));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        chariot.setStatus(ChariotStatus.IN_USE);
        chariot.setLastUsedBy(user);
        chariot.setLastUsedAt(LocalDateTime.now());

        Chariot saved = chariotRepository.save(chariot);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "Chariot assigned"));
    }

    @PutMapping("/{id}/release")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> releaseChariot(@PathVariable UUID id) {
        Chariot chariot = chariotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + id));

        chariot.setStatus(ChariotStatus.AVAILABLE);
        Chariot saved = chariotRepository.save(chariot);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "Chariot released"));
    }

    @PutMapping("/{id}/maintenance")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setChariotMaintenance(@PathVariable UUID id) {
        Chariot chariot = chariotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chariot not found: " + id));

        chariot.setStatus(ChariotStatus.MAINTENANCE);
        Chariot saved = chariotRepository.save(chariot);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "Chariot set to maintenance"));
    }

    private Map<String, Object> toMap(Chariot c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("code", c.getCode());
        map.put("status", c.getStatus());
        map.put("active", c.getActive());
        map.put("lastUsedAt", c.getLastUsedAt());
        if (c.getCurrentLocation() != null) {
            map.put("currentLocation", Map.of(
                    "id", c.getCurrentLocation().getId(),
                    "code", c.getCurrentLocation().getCode()));
        }
        if (c.getLastUsedBy() != null) {
            map.put("lastUsedBy", Map.of(
                    "id", c.getLastUsedBy().getId(),
                    "username", c.getLastUsedBy().getUsername()));
        }
        return map;
    }
}
