package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.enums.LocationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "emplacements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_emplacement")
    private UUID id;

    @Column(name = "code_emplacement", nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_entrepot", nullable = false)
    private Warehouse warehouse;

    @Column(length = 50)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_emplacement", nullable = false, length = 20)
    private LocationType type;

    @Column
    private Double positionX;

    @Column
    private Double positionY;

    @Column
    @Builder.Default
    private Double positionZ = 0.0;

    @Column
    private Double distanceFromReceipt;

    @Column
    private Double distanceFromExpedition;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "`Volume (m3)`")
    private Double volumeM3;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sourceLocation")
    @Builder.Default
    private List<TransactionLine> sourceTransactionLines = new ArrayList<>();

    @OneToMany(mappedBy = "destinationLocation")
    @Builder.Default
    private List<TransactionLine> destinationTransactionLines = new ArrayList<>();

    @OneToMany(mappedBy = "location")
    @Builder.Default
    private List<StockLedger> stockLedgers = new ArrayList<>();

    @OneToMany(mappedBy = "currentLocation")
    @Builder.Default
    private List<Chariot> chariots = new ArrayList<>();

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
