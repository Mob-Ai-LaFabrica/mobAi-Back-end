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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "code_barre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code_barres", nullable = false, unique = true, length = 100)
    private String barcode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produit", nullable = false)
    private Product product;

    @Column(name = "type_code_barres", length = 50)
    private String typeCodBarres;

    @Column(name = "principal", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "`code barre fardeau`", length = 100)
    private String codeBarreFardeau;

    @Column(name = "`code barre palette`", length = 100)
    private String codeBarrePalette;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
