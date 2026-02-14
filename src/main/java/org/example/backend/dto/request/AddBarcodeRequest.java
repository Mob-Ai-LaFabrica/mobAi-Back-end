package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddBarcodeRequest {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @Builder.Default
    private Boolean isPrimary = false;
}
