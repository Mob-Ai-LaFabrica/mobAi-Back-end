package org.example.backend.repository;

import org.example.backend.entity.AiDecisionLog;
import org.example.backend.enums.AiDecisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiDecisionLogRepository extends JpaRepository<AiDecisionLog, UUID> {

    List<AiDecisionLog> findByOverriddenAtIsNull();

    @Query("SELECT a FROM AiDecisionLog a WHERE " +
            "(:decisionType IS NULL OR a.decisionType = :decisionType) " +
            "AND (:wasOverridden IS NULL OR a.wasOverridden = :wasOverridden)")
    Page<AiDecisionLog> findWithFilters(
            @Param("decisionType") AiDecisionType decisionType,
            @Param("wasOverridden") Boolean wasOverridden,
            Pageable pageable);

    long countByWasOverridden(Boolean wasOverridden);
}
