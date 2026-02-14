package org.example.backend.dto.response;

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
public class OrderResponse {

    private String idCommandeAchat;
    private UUID productId;
    private String productSku;
    private Integer quantiteCommandee;
    private LocalDate dateReceptionPrevue;
    private String statut;
}