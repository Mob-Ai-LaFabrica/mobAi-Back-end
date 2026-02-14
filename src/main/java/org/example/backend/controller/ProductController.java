package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.entity.Product;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllProducts() {
        List<Map<String, Object>> products = productRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProduct(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(product), "Product retrieved"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchProducts(@RequestParam String query) {
        List<Map<String, Object>> results = productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(results, "Products matching '" + query + "'"));
    }

    private Map<String, Object> toMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("sku", p.getSku());
        map.put("name", p.getName());
        map.put("unitOfMeasure", p.getUnitOfMeasure());
        map.put("category", p.getCategory());
        map.put("active", p.getActive());
        return map;
    }
}
