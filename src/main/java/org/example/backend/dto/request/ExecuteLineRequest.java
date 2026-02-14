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
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteLineRequest {

    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotNull(message = "Line number is required")
    private Integer lineNumber;

    @NotBlank(message = "Product barcode is required")
    private String productBarcode;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String sourceLocationCode;

    private String destinationLocationCode;
}