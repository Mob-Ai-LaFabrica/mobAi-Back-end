package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(length = 100)
    private String category;

    @Column
    private Double price;

    @Column
    private Integer minStock;

    @Column
    private Integer maxStock;

    @Column(nullable = false)
    private Integer colisageFardeau;

    @Column
    private Integer colisagePalette;

    @Column(nullable = false)
    private Double volumePcs;

    @Column
    private Double poids;

    @Column
    private Boolean isGerbable;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<ProductBarcode> barcodes = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<TransactionLine> transactionLines = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<StockLedger> stockLedgers = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<DemandHistory> demandHistory = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<TaskDiscrepancy> discrepancies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
