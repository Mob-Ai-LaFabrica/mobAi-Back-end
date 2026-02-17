package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.AddBarcodeRequest;
import org.example.backend.dto.request.ProductCreateRequest;
import org.example.backend.dto.request.UpdateProductRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Product;
import org.example.backend.entity.ProductBarcode;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ProductBarcodeRepository;
import org.example.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductRepository productRepository;
    private final ProductBarcodeRepository productBarcodeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Product> products = productRepository.findWithFilters(category, active, search,
                PageRequest.of(page, size));

        List<Map<String, Object>> content = products.getContent().stream().map(this::toProductMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", products.getTotalElements());
        response.put("totalPages", products.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductById(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Map<String, Object> response = toProductMap(product);
        response.put("barcodes", product.getBarcodes().stream().map(b -> Map.of(
                "id", b.getId(),
                "barcode", b.getBarcode(),
                "isPrimary", b.getIsPrimary())).toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("SKU already exists: " + request.getSku()));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .unitOfMeasure(request.getUnitOfMeasure())
                .category(request.getCategory())
                .price(request.getPrice())
                .minStock(request.getMinStock())
                .maxStock(request.getMaxStock())
                .colisageFardeau(request.getColisageFardeau())
                .colisagePalette(request.getColisagePalette())
                .volumePcs(request.getVolumePcs())
                .poids(request.getPoids())
                .isGerbable(request.getIsGerbable())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .build();

        product = productRepository.save(product);

        // Add barcodes if provided
        if (request.getBarcodes() != null && !request.getBarcodes().isEmpty()) {
            for (ProductCreateRequest.BarcodeEntry entry : request.getBarcodes()) {
                ProductBarcode barcode = ProductBarcode.builder()
                        .barcode(entry.getBarcode())
                        .product(product)
                        .isPrimary(entry.getIsPrimary() != null ? entry.getIsPrimary() : false)
                        .build();
                productBarcodeRepository.save(barcode);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toProductMap(product), "Product created successfully"));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getCategory() != null)
            product.setCategory(request.getCategory());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getMinStock() != null)
            product.setMinStock(request.getMinStock());
        if (request.getMaxStock() != null)
            product.setMaxStock(request.getMaxStock());
        if (request.getColisageFardeau() != null)
            product.setColisageFardeau(request.getColisageFardeau());
        if (request.getColisagePalette() != null)
            product.setColisagePalette(request.getColisagePalette());
        if (request.getVolumePcs() != null)
            product.setVolumePcs(request.getVolumePcs());
        if (request.getPoids() != null)
            product.setPoids(request.getPoids());
        if (request.getIsGerbable() != null)
            product.setIsGerbable(request.getIsGerbable());
        if (request.getActive() != null)
            product.setActive(request.getActive());
        if (request.getQuantity() != null)
            product.setQuantity(request.getQuantity());

        product = productRepository.save(product);

        return ResponseEntity.ok(ApiResponse.success(toProductMap(product), "Product updated successfully"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteProduct(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setActive(false);
        productRepository.save(product);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Product deleted successfully"), "Product deleted successfully"));
    }

    @PostMapping("/{productId}/barcodes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addBarcode(
            @PathVariable UUID productId,
            @Valid @RequestBody AddBarcodeRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductBarcode barcode = ProductBarcode.builder()
                .barcode(request.getBarcode())
                .product(product)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .build();

        barcode = productBarcodeRepository.save(barcode);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", barcode.getId());
        response.put("barcode", barcode.getBarcode());
        response.put("isPrimary", barcode.getIsPrimary());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Barcode added successfully"));
    }

    @DeleteMapping("/{productId}/barcodes/{barcodeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeBarcode(
            @PathVariable UUID productId,
            @PathVariable UUID barcodeId) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductBarcode barcode = productBarcodeRepository.findById(barcodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode not found: " + barcodeId));

        productBarcodeRepository.delete(barcode);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Barcode removed successfully"), "Barcode removed successfully"));
    }

    private Map<String, Object> toProductMap(Product product) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", product.getId());
        map.put("sku", product.getSku());
        map.put("name", product.getName());
        map.put("unitOfMeasure", product.getUnitOfMeasure());
        map.put("category", product.getCategory());
        map.put("price", product.getPrice());
        map.put("minStock", product.getMinStock());
        map.put("maxStock", product.getMaxStock());
        map.put("colisageFardeau", product.getColisageFardeau());
        map.put("colisagePalette", product.getColisagePalette());
        map.put("volumePcs", product.getVolumePcs());
        map.put("poids", product.getPoids());
        map.put("isGerbable", product.getIsGerbable());
        map.put("active", product.getActive());
        map.put("quantity", product.getQuantity());
        map.put("createdAt", product.getCreatedAt());
        map.put("updatedAt", product.getUpdatedAt());
        return map;
    }
}
