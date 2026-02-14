package org.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    private String name;

    private String category;

    private Double price;

    private Integer minStock;

    private Integer maxStock;

    private Integer colisageFardeau;

    private Integer colisagePalette;

    private Double volumePcs;

    private Double poids;

    private Boolean isGerbable;

    private Boolean active;
}
