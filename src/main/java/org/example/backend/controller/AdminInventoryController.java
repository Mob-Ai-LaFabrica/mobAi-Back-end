package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.StockAdjustmentRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Location;
import org.example.backend.entity.Product;
import org.example.backend.entity.StockLedger;
import org.example.backend.entity.Transaction;
import org.example.backend.entity.User;
import org.example.backend.enums.MovementType;
import org.example.backend.enums.TransactionStatus;
import org.example.backend.enums.TransactionType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.StockLedgerRepository;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventorySummary(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        var products = productRepository.findWithFilters(category, true, null, PageRequest.of(page, size));
        int lowStockCount = 0;

        List<Map<String, Object>> content = new ArrayList<>();
        for (Product product : products.getContent()) {
            List<StockLedger> ledgers = stockLedgerRepository.findByProduct_Id(product.getId());

            // Calculate total stock by summing running balances per location
            Map<UUID, Integer> stockByLocation = new LinkedHashMap<>();
            for (StockLedger ledger : ledgers) {
                stockByLocation.merge(ledger.getLocation().getId(), ledger.getQuantity() *
                        (ledger.getMovementType() == MovementType.OUT ? -1 : 1), Integer::sum);
            }

            int totalStock = stockByLocation.values().stream().mapToInt(Integer::intValue).sum();
            boolean isLowStock = product.getMinStock() != null && totalStock < product.getMinStock();
            if (isLowStock)
                lowStockCount++;

            if (Boolean.TRUE.equals(lowStockOnly) && !isLowStock)
                continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", product.getId());
            item.put("sku", product.getSku());
            item.put("name", product.getName());
            item.put("category", product.getCategory());
            item.put("price", product.getPrice());
            item.put("unitOfMeasure", product.getUnitOfMeasure());
            item.put("totalStock", totalStock);
            item.put("minStock", product.getMinStock());
            item.put("maxStock", product.getMaxStock());
            item.put("stockAlert", isLowStock);

            List<Map<String, Object>> stockLocations = stockByLocation.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(e -> {
                        Location loc = locationRepository.findById(e.getKey()).orElse(null);
                        Map<String, Object> locMap = new LinkedHashMap<>();
                        locMap.put("locationCode", loc != null ? loc.getCode() : e.getKey().toString());
                        locMap.put("quantity", e.getValue());
                        return locMap;
                    }).collect(Collectors.toList());
            item.put("stockLocations", stockLocations);
            content.add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalProducts", products.getTotalElements());
        response.put("lowStockCount", lowStockCount);

        return ResponseEntity.ok(ApiResponse.success(response, "Inventory summary retrieved"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockAlerts() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getActive() && p.getMinStock() != null)
                .toList();

        List<Map<String, Object>> alerts = new ArrayList<>();
        for (Product product : products) {
            List<StockLedger> ledgers = stockLedgerRepository.findByProduct_Id(product.getId());
            int totalStock = ledgers.stream()
                    .mapToInt(l -> l.getMovementType() == MovementType.OUT ? -l.getQuantity() : l.getQuantity()).sum();

            if (totalStock < product.getMinStock()) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("productId", product.getId());
                alert.put("sku", product.getSku());
                alert.put("name", product.getName());
                alert.put("currentStock", totalStock);
                alert.put("minStock", product.getMinStock());
                alert.put("deficit", product.getMinStock() - totalStock);
                alerts.add(alert);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(alerts, "Stock alerts retrieved"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductInventory(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        List<StockLedger> ledgers = stockLedgerRepository.findByProduct_Id(productId);

        Map<UUID, Integer> stockByLocation = new LinkedHashMap<>();
        for (StockLedger ledger : ledgers) {
            stockByLocation.merge(ledger.getLocation().getId(), ledger.getQuantity() *
                    (ledger.getMovementType() == MovementType.OUT ? -1 : 1), Integer::sum);
        }
        int totalStock = stockByLocation.values().stream().mapToInt(Integer::intValue).sum();

        List<Map<String, Object>> stockLocations = stockByLocation.entrySet().stream()
                .map(e -> {
                    Location loc = locationRepository.findById(e.getKey()).orElse(null);
                    Map<String, Object> locMap = new LinkedHashMap<>();
                    locMap.put("locationId", e.getKey());
                    locMap.put("locationCode", loc != null ? loc.getCode() : "UNKNOWN");
                    locMap.put("quantity", e.getValue());
                    return locMap;
                }).collect(Collectors.toList());

        // Recent movements
        List<Map<String, Object>> movements = ledgers.stream()
                .sorted(Comparator.comparing(StockLedger::getPerformedAt).reversed())
                .limit(20)
                .map(l -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", l.getId());
                    map.put("movementType", l.getMovementType().name());
                    map.put("quantity", l.getQuantity());
                    map.put("locationCode", l.getLocation().getCode());
                    map.put("performedBy", l.getPerformedBy().getFullName());
                    map.put("performedAt", l.getPerformedAt());
                    return map;
                }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("product", Map.of(
                "id", product.getId(),
                "sku", product.getSku(),
                "name", product.getName()));
        response.put("totalStock", totalStock);
        response.put("stockByLocation", stockLocations);
        response.put("stockMovements", movements);

        return ResponseEntity.ok(ApiResponse.success(response, "Product inventory retrieved"));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLocationInventory(@PathVariable UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));

        List<UUID> productIds = stockLedgerRepository.findDistinctProductIdsByLocationId(locationId);

        List<Map<String, Object>> products = productIds.stream().map(pid -> {
            Product product = productRepository.findById(pid).orElse(null);
            if (product == null)
                return null;

            List<StockLedger> ledgers = stockLedgerRepository.findByProduct_IdAndLocation_Id(pid, locationId);
            int quantity = ledgers.stream()
                    .mapToInt(l -> l.getMovementType() == MovementType.OUT ? -l.getQuantity() : l.getQuantity()).sum();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", product.getId());
            map.put("sku", product.getSku());
            map.put("name", product.getName());
            map.put("quantity", quantity);
            return map;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("location", Map.of(
                "id", location.getId(),
                "code", location.getCode(),
                "zone", location.getZone() != null ? location.getZone() : "",
                "type", location.getType().name()));
        response.put("products", products);

        return ResponseEntity.ok(ApiResponse.success(response, "Location inventory retrieved"));
    }

    @PostMapping("/adjustment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStockAdjustment(
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.getLocationId()));
        User admin = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create adjustment transaction
        Transaction transaction = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .reference("ADJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(TransactionStatus.COMPLETED)
                .createdBy(admin)
                .notes("Stock adjustment: " + request.getReason() +
                        (request.getNotes() != null ? " - " + request.getNotes() : ""))
                .build();
        transaction = transactionRepository.save(transaction);

        // Determine movement type
        MovementType movementType = request.getAdjustmentQuantity() >= 0 ? MovementType.IN : MovementType.ADJUSTMENT;
        int qty = Math.abs(request.getAdjustmentQuantity());

        // Calculate new balance
        Long currentBalance = stockLedgerRepository.calculateBalance(request.getProductId(), request.getLocationId());
        int newBalance = (int) (currentBalance + request.getAdjustmentQuantity());

        StockLedger ledger = StockLedger.builder()
                .product(product)
                .location(location)
                .transaction(transaction)
                .movementType(movementType)
                .quantity(qty)
                .runningBalance(Math.max(0, newBalance))
                .performedBy(admin)
                .performedAt(LocalDateTime.now())
                .build();
        stockLedgerRepository.save(ledger);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", transaction.getId());
        response.put("newBalance", Math.max(0, newBalance));
        response.put("message", "Stock adjusted successfully");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock adjusted successfully"));
    }
}
