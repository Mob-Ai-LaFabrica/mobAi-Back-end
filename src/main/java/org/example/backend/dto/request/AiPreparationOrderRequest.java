package org.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPreparationOrderRequest {

    private String date;

    @JsonProperty("override_quantities")
    private Map<Integer, Double> overrideQuantities;
}
