package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.AiDecisionOverrideRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.AiDecisionLog;
import org.example.backend.entity.User;
import org.example.backend.enums.AiDecisionType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.AiDecisionLogRepository;
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
@RequestMapping("/admin/ai-decisions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAiDecisionController {

    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllAiDecisions(
            @RequestParam(required = false) AiDecisionType decisionType,
            @RequestParam(required = false) Boolean wasOverridden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AiDecisionLog> decisions = aiDecisionLogRepository.findWithFilters(
                decisionType, wasOverridden, PageRequest.of(page, size));

        List<Map<String, Object>> content = decisions.getContent().stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("decisionType", d.getDecisionType().name());
            map.put("entityType", d.getEntityType());
            map.put("entityId", d.getEntityId());
            map.put("aiSuggestion", d.getAiSuggestion());
            map.put("finalDecision", d.getFinalDecision());
            map.put("wasOverridden", d.getWasOverridden());
            map.put("confidence", d.getConfidence());
            map.put("overriddenBy", d.getOverriddenBy() != null ? Map.of(
                    "id", d.getOverriddenBy().getId(),
                    "username", d.getOverriddenBy().getUsername()) : null);
            map.put("overrideReason", d.getOverrideReason());
            map.put("createdAt", d.getCreatedAt());
            map.put("overriddenAt", d.getOverriddenAt());
            return map;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", decisions.getTotalElements());
        response.put("totalPages", decisions.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "AI decisions retrieved"));
    }

    @PutMapping("/{decisionId}/override")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overrideAiDecision(
            @PathVariable UUID decisionId,
            @Valid @RequestBody AiDecisionOverrideRequest request,
            Authentication authentication) {

        AiDecisionLog decision = aiDecisionLogRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("AI decision not found: " + decisionId));

        User admin = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        decision.setFinalDecision(request.getNewDecision());
        decision.setWasOverridden(true);
        decision.setOverriddenBy(admin);
        decision.setOverrideReason(request.getReason());
        decision.setOverriddenAt(LocalDateTime.now());

        decision = aiDecisionLogRepository.save(decision);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("decisionId", decision.getId());
        response.put("wasOverridden", true);
        response.put("overriddenBy", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "fullName", admin.getFullName()));
        response.put("overriddenAt", decision.getOverriddenAt());

        return ResponseEntity.ok(ApiResponse.success(response, "AI decision overridden successfully"));
    }
}
