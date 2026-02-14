package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.TransactionLine;
import org.example.backend.entity.User;
import org.example.backend.exception.InvalidOperationException;
import org.example.backend.service.ReceiptService;
import org.example.backend.service.StockLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final StockLedgerService stockLedgerService;

    @Override
    public void processReceiptLine(Transaction transaction, TransactionLine line, User performer) {
        if (line.getDestinationLocation() == null) {
            throw new InvalidOperationException(
                    "Receipt line " + line.getLineNumber() + " must have a destination location");
        }

        log.info("Processing receipt line {}: product={}, qty={}, dest={}",
                line.getLineNumber(), line.getProduct().getSku(),
                line.getQuantity(), line.getDestinationLocation().getCode());

        // Receipt = stock IN at the destination (receiving) location
        stockLedgerService.recordStockIn(
                line.getProduct(),
                line.getDestinationLocation(),
                line.getQuantity(),
                transaction,
                line,
                performer);
    }

    @Override
    public void processReceipt(Transaction transaction, User performer) {
        log.info("Processing full receipt transaction: {}", transaction.getReference());
        for (TransactionLine line : transaction.getTransactionLines()) {
            processReceiptLine(transaction, line, performer);
        }
    }
}
