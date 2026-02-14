package org.example.backend.repository;

import org.example.backend.entity.Transaction;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAssignedTo_Username(String username);

    List<Transaction> findByAssignedTo_UsernameAndStatus(String username, TransactionStatus status);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByAssignedTo_Id(UUID userId);

    List<Transaction> findByCreatedBy_Id(UUID userId);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:type IS NULL OR t.type = :type) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) " +
            "AND (:createdById IS NULL OR t.createdBy.id = :createdById)")
    Page<Transaction> findWithFilters(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("assignedToId") UUID assignedToId,
            @Param("createdById") UUID createdById,
            Pageable pageable);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status AND t.completedAt >= :since")
    long countByStatusAndCompletedAtAfter(
            @Param("status") TransactionStatus status,
            @Param("since") LocalDateTime since);
}
