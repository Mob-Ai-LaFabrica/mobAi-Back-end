package org.example.backend.repository;

import org.example.backend.entity.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, UUID> {

        List<StockLedger> findByProduct_Id(UUID productId);

        List<StockLedger> findByLocation_Id(UUID locationId);

        List<StockLedger> findByProduct_IdAndLocation_Id(UUID productId, UUID locationId);

        @Query("SELECT COALESCE(SUM(CASE WHEN s.movementType = 'IN' THEN s.quantity " +
                        "WHEN s.movementType = 'OUT' THEN -s.quantity " +
                        "ELSE s.quantity END), 0) " +
                        "FROM StockLedger s WHERE s.product.id = :productId AND s.location.id = :locationId")
        Long calculateBalance(@Param("productId") UUID productId, @Param("locationId") UUID locationId);

        @Query("SELECT s FROM StockLedger s WHERE " +
                        "(:productId IS NULL OR s.product.id = :productId) " +
                        "AND s.performedAt >= :startDate AND s.performedAt <= :endDate " +
                        "ORDER BY s.performedAt DESC")
        List<StockLedger> findMovementsByDateRange(
                        @Param("productId") UUID productId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT DISTINCT s.product.id FROM StockLedger s WHERE s.location.id = :locationId")
        List<UUID> findDistinctProductIdsByLocationId(@Param("locationId") UUID locationId);

        /**
         * Get the latest stock ledger entry for a product at a location (for running
         * balance).
         * Uses pessimistic write lock for concurrency safety.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM StockLedger s WHERE s.product.id = :productId AND s.location.id = :locationId " +
                        "ORDER BY s.performedAt DESC, s.createdAt DESC LIMIT 1")
        Optional<StockLedger> findLatestByProductAndLocationForUpdate(
                        @Param("productId") UUID productId,
                        @Param("locationId") UUID locationId);

        /**
         * Get the latest stock ledger entry without lock (for reads).
         */
        @Query("SELECT s FROM StockLedger s WHERE s.product.id = :productId AND s.location.id = :locationId " +
                        "ORDER BY s.performedAt DESC, s.createdAt DESC LIMIT 1")
        Optional<StockLedger> findLatestByProductAndLocation(
                        @Param("productId") UUID productId,
                        @Param("locationId") UUID locationId);

        /**
         * Find all stock entries by transaction.
         */
        List<StockLedger> findByTransaction_Id(UUID transactionId);
}
