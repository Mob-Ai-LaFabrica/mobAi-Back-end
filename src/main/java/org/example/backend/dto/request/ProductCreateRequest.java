package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String unitOfMeasure;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private Double price;

    private Integer minStock;

    private Integer maxStock;

    @jakarta.validation.constraints.NotNull(message = "Colisage fardeau is required")
    private Integer colisageFardeau;

    private Integer colisagePalette;

    @jakarta.validation.constraints.NotNull(message = "Volume per piece is required")
    private Double volumePcs;

    private Double poids;

    private Boolean isGerbable;

    private Integer quantity = 0;

    private List<BarcodeEntry> barcodes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BarcodeEntry {
        @NotBlank(message = "Barcode is required")
        private String barcode;
        private Boolean isPrimary = false;
    }
}
