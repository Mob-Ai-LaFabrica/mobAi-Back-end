package org.example.backend.service;

import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;

/**
 * Service for Delivery operations.
 * Delivery = final expedition of products (stock OUT from picking/expedition
 * location).
 */
public interface DeliveryService {

    /**
     * Process a single delivery line: stock OUT from source location.
     */
    void processDeliveryLine(Transaction transaction, TransactionLine line, User performer);

    /**
     * Process all lines of a delivery transaction.
     */
    void processDelivery(Transaction transaction, User performer);
}
