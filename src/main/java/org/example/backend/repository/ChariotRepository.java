package org.example.backend.repository;

import org.example.backend.entity.Chariot;
import org.example.backend.enums.ChariotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChariotRepository extends JpaRepository<Chariot, UUID> {

    Optional<Chariot> findByCode(String code);

    boolean existsByCode(String code);

    List<Chariot> findByStatus(ChariotStatus status);

    List<Chariot> findByActive(Boolean active);

    long countByStatus(ChariotStatus status);
}