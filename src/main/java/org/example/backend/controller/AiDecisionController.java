package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.AiDecisionOverrideRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.AiDecisionLog;
import org.example.backend.entity.User;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.AiDecisionLogRepository;
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
@RequestMapping("/ai-decisions")
@RequiredArgsConstructor
public class AiDecisionController {

    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllDecisions() {
        List<Map<String, Object>> decisions = aiDecisionLogRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(decisions, "AI decisions retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDecision(@PathVariable UUID id) {
        AiDecisionLog decision = aiDecisionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI decision not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(decision), "AI decision retrieved"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveDecision(
            @PathVariable UUID id,
            Authentication authentication) {

        AiDecisionLog decision = aiDecisionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI decision not found: " + id));

        User reviewer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        decision.setOverriddenAt(LocalDateTime.now());
        decision.setWasOverridden(false);
        decision.setOverriddenBy(reviewer);
        decision.setOverrideReason("Approved by supervisor");

        AiDecisionLog saved = aiDecisionLogRepository.save(decision);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "AI decision approved"));
    }

    @PostMapping("/{id}/override")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overrideDecision(
            @PathVariable UUID id,
            @Valid @RequestBody AiDecisionOverrideRequest request,
            Authentication authentication) {

        AiDecisionLog decision = aiDecisionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI decision not found: " + id));

        User reviewer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        decision.setWasOverridden(true);
        decision.setOverriddenBy(reviewer);
        decision.setOverrideReason(request.getReason());
        decision.setFinalDecision(request.getNewDecision());
        decision.setOverriddenAt(LocalDateTime.now());

        AiDecisionLog saved = aiDecisionLogRepository.save(decision);
        return ResponseEntity.ok(ApiResponse.success(toMap(saved), "AI decision overridden"));
    }

    @GetMapping("/pending-review")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingReview() {
        List<Map<String, Object>> decisions = aiDecisionLogRepository.findByOverriddenAtIsNull().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(decisions, "Pending AI decisions retrieved"));
    }

    private Map<String, Object> toMap(AiDecisionLog d) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", d.getId());
        map.put("decisionType", d.getDecisionType());
        map.put("entityType", d.getEntityType());
        map.put("entityId", d.getEntityId());
        map.put("aiSuggestion", d.getAiSuggestion());
        map.put("finalDecision", d.getFinalDecision());
        map.put("wasOverridden", d.getWasOverridden());
        map.put("overrideReason", d.getOverrideReason());
        map.put("confidence", d.getConfidence());
        map.put("createdAt", d.getCreatedAt());
        map.put("overriddenAt", d.getOverriddenAt());
        if (d.getOverriddenBy() != null) {
            map.put("overriddenBy", Map.of(
                    "id", d.getOverriddenBy().getId(),
                    "username", d.getOverriddenBy().getUsername()));
        }
        return map;
    }
}
