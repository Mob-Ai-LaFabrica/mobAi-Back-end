package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.enums.AiDecisionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_decision_logs", indexes = {
        @Index(name = "idx_ai_decision_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AiDecisionType decisionType;

    @Column(nullable = false, length = 50, name = "entity_type")
    private String entityType;

    @Column(nullable = false, name = "entity_id")
    private UUID entityId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiSuggestion;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String finalDecision;

    @Column(nullable = false)
    @Builder.Default
    private Boolean wasOverridden = false;

    @ManyToOne
    @JoinColumn(name = "overridden_by_id")
    private User overriddenBy;

    @Column(length = 500)
    private String overrideReason;

    @Column
    private Double confidence;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime overriddenAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
