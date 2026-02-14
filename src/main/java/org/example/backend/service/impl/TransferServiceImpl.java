package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.exception.InvalidOperationException;
import org.example.backend.service.StockLedgerService;
import org.example.backend.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransferServiceImpl implements TransferService {

    private final StockLedgerService stockLedgerService;

    @Override
    public void processTransferLine(Transaction transaction, TransactionLine line, User performer) {
        if (line.getSourceLocation() == null) {
            throw new InvalidOperationException(
                    "Transfer line " + line.getLineNumber() + " must have a source location");
        }
        if (line.getDestinationLocation() == null) {
            throw new InvalidOperationException(
                    "Transfer line " + line.getLineNumber() + " must have a destination location");
        }

        log.info("Processing transfer line {}: product={}, qty={}, from={} -> to={}",
                line.getLineNumber(), line.getProduct().getSku(), line.getQuantity(),
                line.getSourceLocation().getCode(), line.getDestinationLocation().getCode());

        // Validate stock availability at source before moving
        stockLedgerService.validateStockAvailability(
                line.getProduct().getId(),
                line.getSourceLocation().getId(),
                line.getQuantity());

        // Stock OUT from source location
        stockLedgerService.recordStockOut(
                line.getProduct(),
                line.getSourceLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);

        // Stock IN at destination location
        stockLedgerService.recordStockIn(
                line.getProduct(),
                line.getDestinationLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);
    }

    @Override
    public void processTransfer(Transaction transaction, User performer) {
        log.info("Processing full transfer transaction: {}", transaction.getReference());
        for (TransactionLine line : transaction.getTransactionLines()) {
            processTransferLine(transaction, line, performer);
        }
    }
}
