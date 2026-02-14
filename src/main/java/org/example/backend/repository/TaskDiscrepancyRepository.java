package org.example.backend.repository;

import org.example.backend.entity.TaskDiscrepancy;
import org.example.backend.enums.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDiscrepancyRepository extends JpaRepository<TaskDiscrepancy, UUID> {

    List<TaskDiscrepancy> findByResolvedAtIsNull();

    @Query("SELECT d FROM TaskDiscrepancy d WHERE " +
            "(:resolved IS NULL OR (:resolved = true AND d.resolvedAt IS NOT NULL) OR (:resolved = false AND d.resolvedAt IS NULL)) "
            +
            "AND (:issueType IS NULL OR d.issueType = :issueType)")
    Page<TaskDiscrepancy> findWithFilters(
            @Param("resolved") Boolean resolved,
            @Param("issueType") IssueType issueType,
            Pageable pageable);

    long countByResolvedAtIsNull();
}