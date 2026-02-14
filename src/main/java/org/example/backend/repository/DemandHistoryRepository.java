package org.example.backend.repository;

import org.example.backend.entity.DemandHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DemandHistoryRepository extends JpaRepository<DemandHistory, UUID> {

    List<DemandHistory> findByProduct_Id(UUID productId);

    List<DemandHistory> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<DemandHistory> findByProduct_IdAndDateBetween(UUID productId, LocalDate startDate, LocalDate endDate);
}
