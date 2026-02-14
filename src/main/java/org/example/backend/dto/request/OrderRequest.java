package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Purchase order ID is required")
    private String idCommandeAchat;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Ordered quantity is required")
    private Integer quantiteCommandee;

    @NotNull(message = "Expected reception date is required")
    private LocalDate dateReceptionPrevue;

    @Builder.Default
    private String statut = "OPEN";
}