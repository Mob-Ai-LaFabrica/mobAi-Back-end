package org.example.backend.repository;

import org.example.backend.entity.TransactionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionLineRepository extends JpaRepository<TransactionLine, UUID> {

    Optional<TransactionLine> findByTransaction_IdAndLineNumber(UUID transactionId, Integer lineNumber);
}
