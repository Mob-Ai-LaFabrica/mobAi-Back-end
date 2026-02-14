package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.ResolveDiscrepancyRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.TaskDiscrepancy;
import org.example.backend.entity.User;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.TaskDiscrepancyRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/discrepancies")
@RequiredArgsConstructor
public class DiscrepancyController {

    private final TaskDiscrepancyRepository discrepancyRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllDiscrepancies() {
        List<Map<String, Object>> result = discrepancyRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Discrepancies retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDiscrepancy(@PathVariable UUID id) {
        TaskDiscrepancy d = discrepancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discrepancy not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(d), "Discrepancy retrieved"));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resolveDiscrepancy(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDiscrepancyRequest request,
            Authentication authentication) {

        TaskDiscrepancy d = discrepancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discrepancy not found: " + id));

        User resolver = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        d.setResolvedBy(resolver);
        d.setResolvedAt(LocalDateTime.now());
        d.setResolution(request.getResolution());
        if (request.getNotes() != null) {
            d.setNotes(request.getNotes());
        }

        TaskDiscrepancy saved = discrepancyRepository.save(d);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "Discrepancy resolved"));
    }

    @GetMapping("/unresolved")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnresolved() {
        List<Map<String, Object>> result = discrepancyRepository.findByResolvedAtIsNull().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Unresolved discrepancies retrieved"));
    }

    private Map<String, Object> toMap(TaskDiscrepancy d) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", d.getId());
        map.put("transactionId", d.getTransaction() != null ? d.getTransaction().getId() : null);
        map.put("productId", d.getProduct() != null ? d.getProduct().getId() : null);
        map.put("productSku", d.getProduct() != null ? d.getProduct().getSku() : null);
        map.put("issueType", d.getIssueType());
        map.put("expectedQuantity", d.getExpectedQuantity());
        map.put("actualQuantity", d.getActualQuantity());
        map.put("reportedAt", d.getReportedAt());
        map.put("notes", d.getNotes());
        map.put("resolution", d.getResolution());
        map.put("resolvedAt", d.getResolvedAt());
        if (d.getReportedBy() != null) {
            map.put("reportedBy", Map.of(
                    "id", d.getReportedBy().getId(),
                    "username", d.getReportedBy().getUsername()));
        }
        if (d.getResolvedBy() != null) {
            map.put("resolvedBy", Map.of(
                    "id", d.getResolvedBy().getId(),
                    "username", d.getResolvedBy().getUsername()));
        }
        return map;
    }
}
