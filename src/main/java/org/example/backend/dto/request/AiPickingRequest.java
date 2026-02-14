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
public class AiPickingRequest {

    private List<AiPickingItem> items;

    @JsonProperty("chariot_capacity_kg")
    @Builder.Default
    private double chariotCapacityKg = 300.0;

    @JsonProperty("max_chariots")
    @Builder.Default
    private int maxChariots = 3;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiPickingItem {
        @JsonProperty("product_id")
        private int productId;

        private int quantity;

        @JsonProperty("location_id")
        private Integer locationId;
    }
}
