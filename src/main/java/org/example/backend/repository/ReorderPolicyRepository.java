package org.example.backend.repository;

import org.example.backend.entity.ReorderPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReorderPolicyRepository extends JpaRepository<ReorderPolicy, UUID> {

    Optional<ReorderPolicy> findByProduct_Id(UUID productId);
}
