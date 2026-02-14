package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cmd_achat_ouvertes_opt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenPurchaseOrder {

    @Id
    @Column(name = "id_commande_achat", nullable = false, length = 50)
    private String idCommandeAchat;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produit", nullable = false)
    private Product product;

    @Column(name = "quantite_commandee", nullable = false)
    private Integer quantiteCommandee;

    @Column(name = "date_reception_prevue", nullable = false)
    private LocalDate dateReceptionPrevue;

    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private String statut = "OPEN";
}
