package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.OrderRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.dto.response.OrderResponse;
import org.example.backend.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OperationsController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idCommandeAchat") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<OrderResponse> orders = orderService.getAllOrders(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStatut(
            @PathVariable String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idCommandeAchat") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<OrderResponse> orders = orderService.getOrdersByStatut(statut, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders by status retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderRequest request) {

        OrderResponse order = orderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('operation:write')")
    public ResponseEntity<ApiResponse<String>> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully"));
    }
}
