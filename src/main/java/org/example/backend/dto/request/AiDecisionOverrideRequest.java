package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDecisionOverrideRequest {

    @NotBlank(message = "New decision is required")
    private String newDecision;

    @NotBlank(message = "Override reason is required")
    private String reason;
}
