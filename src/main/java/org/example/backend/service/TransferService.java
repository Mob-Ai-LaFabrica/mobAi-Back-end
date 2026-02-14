package org.example.backend.service;

import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;

/**
 * Service for Transfer operations.
 * Transfer = moving products from source location to destination location.
 * Stock OUT at source, stock IN at destination.
 */
public interface TransferService {

    /**
     * Process a single transfer line: stock OUT from source, stock IN at
     * destination.
     */
    void processTransferLine(Transaction transaction, TransactionLine line, User performer);

    /**
     * Process all lines of a transfer transaction.
     */
    void processTransfer(Transaction transaction, User performer);
}
