package org.example.backend.service;

import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;

/**
 * Service for Receipt operations.
 * Receipt = receiving merchandise into the warehouse (stock IN at destination
 * location).
 */
public interface ReceiptService {

    /**
     * Process a single receipt line: creates stock IN at the destination location.
     */
    void processReceiptLine(Transaction transaction, TransactionLine line, User performer);

    /**
     * Process all lines of a receipt transaction.
     */
    void processReceipt(Transaction transaction, User performer);
}
