package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.*;
import org.example.backend.enums.MovementType;
import org.example.backend.exception.InsufficientStockException;
import org.example.backend.repository.StockLedgerRepository;
import org.example.backend.service.StockLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockLedgerServiceImpl implements StockLedgerService {

    private final StockLedgerRepository stockLedgerRepository;

    @Override
    public StockLedger recordStockIn(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy) {
        log.info("Recording stock IN: product={}, location={}, qty={}, txn={}",
                product.getSku(), location.getCode(), quantity, transaction.getReference());

        int currentBalance = getBalanceWithLock(product.getId(), location.getId());
        int newBalance = currentBalance + quantity;

        return createLedgerEntry(product, location, transaction, transactionLine,
                MovementType.IN, quantity, newBalance, performedBy);
    }

    @Override
    public StockLedger recordStockOut(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy) {
        log.info("Recording stock OUT: product={}, location={}, qty={}, txn={}",
                product.getSku(), location.getCode(), quantity, transaction.getReference());

        int currentBalance = getBalanceWithLock(product.getId(), location.getId());
        int newBalance = currentBalance - quantity;

        if (newBalance < 0) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s at location %s. Available: %d, Requested: %d",
                            product.getSku(), location.getCode(), currentBalance, quantity));
        }

        return createLedgerEntry(product, location, transaction, transactionLine,
                MovementType.OUT, quantity, newBalance, performedBy);
    }

    @Override
    public StockLedger recordAdjustment(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy) {
        log.info("Recording stock ADJUSTMENT: product={}, location={}, qty={}, txn={}",
                product.getSku(), location.getCode(), quantity, transaction.getReference());

        int currentBalance = getBalanceWithLock(product.getId(), location.getId());
        int newBalance = currentBalance + quantity;

        if (newBalance < 0) {
            throw new InsufficientStockException(
                    String.format(
                            "Adjustment would result in negative stock for product %s at location %s. Balance: %d, Adjustment: %d",
                            product.getSku(), location.getCode(), currentBalance, quantity));
        }

        return createLedgerEntry(product, location, transaction, transactionLine,
                MovementType.ADJUSTMENT, quantity, newBalance, performedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCurrentBalance(UUID productId, UUID locationId) {
        return stockLedgerRepository.findLatestByProductAndLocation(productId, locationId)
                .map(StockLedger::getRunningBalance)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateStockAvailability(UUID productId, UUID locationId, int requiredQuantity) {
        int currentBalance = getCurrentBalance(productId, locationId);
        if (currentBalance < requiredQuantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Required: %d", currentBalance, requiredQuantity));
        }
    }

    /**
     * Get balance using pessimistic lock for write operations.
     */
    private int getBalanceWithLock(UUID productId, UUID locationId) {
        return stockLedgerRepository.findLatestByProductAndLocationForUpdate(productId, locationId)
                .map(StockLedger::getRunningBalance)
                .orElse(0);
    }

    private StockLedger createLedgerEntry(Product product, Location location, Transaction transaction,
            TransactionLine transactionLine, MovementType movementType,
            int quantity, int runningBalance, User performedBy) {
        StockLedger entry = StockLedger.builder()
                .product(product)
                .location(location)
                .transaction(transaction)
                .transactionLine(transactionLine)
                .movementType(movementType)
                .quantity(quantity)
                .runningBalance(runningBalance)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();

        return stockLedgerRepository.save(entry);
    }
}
