package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.ResolveDiscrepancyRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.TaskDiscrepancy;
import org.example.backend.entity.User;
import org.example.backend.enums.IssueType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.TaskDiscrepancyRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/discrepancies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDiscrepancyController {

    private final TaskDiscrepancyRepository taskDiscrepancyRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllDiscrepancies(
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) IssueType issueType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TaskDiscrepancy> discrepancies = taskDiscrepancyRepository.findWithFilters(
                resolved, issueType, PageRequest.of(page, size));

        List<Map<String, Object>> content = discrepancies.getContent().stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("issueType", d.getIssueType().name());
            map.put("expectedQuantity", d.getExpectedQuantity());
            map.put("actualQuantity", d.getActualQuantity());
            map.put("transaction", d.getTransaction() != null ? Map.of(
                    "id", d.getTransaction().getId(),
                    "reference", d.getTransaction().getReference()) : null);
            map.put("product", d.getProduct() != null ? Map.of(
                    "id", d.getProduct().getId(),
                    "sku", d.getProduct().getSku(),
                    "name", d.getProduct().getName()) : null);
            map.put("reportedBy", d.getReportedBy() != null ? Map.of(
                    "id", d.getReportedBy().getId(),
                    "username", d.getReportedBy().getUsername()) : null);
            map.put("reportedAt", d.getReportedAt());
            map.put("resolvedBy", d.getResolvedBy() != null ? Map.of(
                    "id", d.getResolvedBy().getId(),
                    "username", d.getResolvedBy().getUsername()) : null);
            map.put("resolvedAt", d.getResolvedAt());
            map.put("resolution", d.getResolution());
            map.put("notes", d.getNotes());
            return map;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", discrepancies.getTotalElements());
        response.put("totalPages", discrepancies.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Discrepancies retrieved"));
    }

    @PutMapping("/{discrepancyId}/resolve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resolveDiscrepancy(
            @PathVariable UUID discrepancyId,
            @Valid @RequestBody ResolveDiscrepancyRequest request,
            Authentication authentication) {

        TaskDiscrepancy discrepancy = taskDiscrepancyRepository.findById(discrepancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Discrepancy not found: " + discrepancyId));

        User admin = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        discrepancy.setResolution(request.getResolution());
        discrepancy.setNotes(request.getNotes());
        discrepancy.setResolvedBy(admin);
        discrepancy.setResolvedAt(LocalDateTime.now());

        discrepancy = taskDiscrepancyRepository.save(discrepancy);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("discrepancyId", discrepancy.getId());
        response.put("resolvedBy", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "fullName", admin.getFullName()));
        response.put("resolvedAt", discrepancy.getResolvedAt());
        response.put("resolution", discrepancy.getResolution());

        return ResponseEntity.ok(ApiResponse.success(response, "Discrepancy resolved successfully"));
    }
}
