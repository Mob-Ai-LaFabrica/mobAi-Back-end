package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Location ID is required")
    private UUID locationId;

    @NotNull(message = "Adjustment quantity is required")
    private Integer adjustmentQuantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}
