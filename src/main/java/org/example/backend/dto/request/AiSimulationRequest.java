package org.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSimulationRequest {

    private List<AiSimulationEvent> events;

    @JsonProperty("reset_state")
    @Builder.Default
    private boolean resetState = true;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiSimulationEvent {
        private String date;

        @JsonProperty("product_id")
        private int productId;

        private int quantity;

        @JsonProperty("flow_type")
        private String flowType;
    }
}
