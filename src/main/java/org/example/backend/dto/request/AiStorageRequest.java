package org.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStorageRequest {

    @JsonProperty("product_id")
    private int productId;

    private int quantity;

    @JsonProperty("weight_kg")
    private Double weightKg;
}
