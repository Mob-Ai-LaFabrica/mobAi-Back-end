package org.example.backend.service;

import org.example.backend.entity.*;
import org.example.backend.enums.MovementType;

import java.util.UUID;

/**
 * Service for managing stock ledger entries (stock movements).
 * All stock mutations MUST go through this service to ensure consistency.
 */
public interface StockLedgerService {

    /**
     * Record a stock IN movement (increases balance at location).
     */
    StockLedger recordStockIn(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy);

    /**
     * Record a stock OUT movement (decreases balance at location).
     * Validates sufficient stock before proceeding.
     * 
     * @throws org.example.backend.exception.InsufficientStockException if balance
     *                                                                  would go
     *                                                                  negative
     */
    StockLedger recordStockOut(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy);

    /**
     * Record a stock adjustment (can be positive or negative).
     */
    StockLedger recordAdjustment(Product product, Location location, int quantity,
            Transaction transaction, TransactionLine transactionLine, User performedBy);

    /**
     * Get current stock balance for a product at a location.
     */
    int getCurrentBalance(UUID productId, UUID locationId);

    /**
     * Validate that sufficient stock exists before an outbound move.
     * 
     * @throws org.example.backend.exception.InsufficientStockException if
     *                                                                  insufficient
     */
    void validateStockAvailability(UUID productId, UUID locationId, int requiredQuantity);
}
