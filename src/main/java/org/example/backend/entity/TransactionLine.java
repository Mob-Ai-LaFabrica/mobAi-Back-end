package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lignes_transaction", uniqueConstraints = {
        @UniqueConstraint(name = "idx_transaction_line", columnNames = { "id_transaction", "no_ligne" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_transaction", nullable = false)
    private Transaction transaction;

    @Column(name = "no_ligne", nullable = false)
    private Integer lineNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produit", nullable = false)
    private Product product;

    @Column(name = "quantite", nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "id_emplacement_source")
    private Location sourceLocation;

    @ManyToOne
    @JoinColumn(name = "id_emplacement_destination")
    private Location destinationLocation;

    @Column(name = "lot_serie", length = 100)
    private String lotSerie;

    @Column(name = "code_motif", length = 100)
    private String codeMotif;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
