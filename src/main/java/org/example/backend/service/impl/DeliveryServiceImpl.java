package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.exception.InvalidOperationException;
import org.example.backend.service.DeliveryService;
import org.example.backend.service.StockLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final StockLedgerService stockLedgerService;

    @Override
    public void processDeliveryLine(Transaction transaction, TransactionLine line, User performer) {
        if (line.getSourceLocation() == null) {
            throw new InvalidOperationException(
                    "Delivery line " + line.getLineNumber() + " must have a source (picking/expedition) location");
        }

        log.info("Processing delivery line {}: product={}, qty={}, from={}",
                line.getLineNumber(), line.getProduct().getSku(),
                line.getQuantity(), line.getSourceLocation().getCode());

        // Validate stock availability at source location
        stockLedgerService.validateStockAvailability(
                line.getProduct().getId(),
                line.getSourceLocation().getId(),
                line.getQuantity());

        // Stock OUT from picking/expedition location (product leaves the warehouse)
        stockLedgerService.recordStockOut(
                line.getProduct(),
                line.getSourceLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);
    }

    @Override
    public void processDelivery(Transaction transaction, User performer) {
        log.info("Processing full delivery transaction: {}", transaction.getReference());
        for (TransactionLine line : transaction.getTransactionLines()) {
            processDeliveryLine(transaction, line, performer);
        }
    }
}
