package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.exception.InvalidOperationException;
import org.example.backend.service.PickingService;
import org.example.backend.service.StockLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PickingServiceImpl implements PickingService {

    private final StockLedgerService stockLedgerService;

    @Override
    public void processPickingLine(Transaction transaction, TransactionLine line, User performer) {
        if (line.getSourceLocation() == null) {
            throw new InvalidOperationException(
                    "Picking line " + line.getLineNumber() + " must have a source (storage) location");
        }
        if (line.getDestinationLocation() == null) {
            throw new InvalidOperationException(
                    "Picking line " + line.getLineNumber() + " must have a destination (picking rack) location");
        }

        log.info("Processing picking line {}: product={}, qty={}, from={} -> to={}",
                line.getLineNumber(), line.getProduct().getSku(), line.getQuantity(),
                line.getSourceLocation().getCode(), line.getDestinationLocation().getCode());

        // Validate stock availability at storage location
        stockLedgerService.validateStockAvailability(
                line.getProduct().getId(),
                line.getSourceLocation().getId(),
                line.getQuantity());

        // Stock OUT from storage location
        stockLedgerService.recordStockOut(
                line.getProduct(),
                line.getSourceLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);

        // Stock IN at picking rack location
        stockLedgerService.recordStockIn(
                line.getProduct(),
                line.getDestinationLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);
    }

    @Override
    public void processPicking(Transaction transaction, User performer) {
        log.info("Processing full picking transaction: {}", transaction.getReference());
        for (TransactionLine line : transaction.getTransactionLines()) {
            processPickingLine(transaction, line, performer);
        }
    }
}
