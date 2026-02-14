package org.example.backend.repository;

import org.example.backend.entity.SupplyLeadTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplyLeadTimeRepository extends JpaRepository<SupplyLeadTime, UUID> {

    Optional<SupplyLeadTime> findByProduct_Id(UUID productId);
}
