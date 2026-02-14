package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.request.OrderRequest;
import org.example.backend.dto.response.OrderResponse;
import org.example.backend.entity.OpenPurchaseOrder;
import org.example.backend.entity.Product;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.OpenPurchaseOrderRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OpenPurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String idCommandeAchat) {
        OpenPurchaseOrder order = getExistingOrder(idCommandeAchat);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatut(String statut, int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return orderRepository.findByStatut(statut, pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        OpenPurchaseOrder order = OpenPurchaseOrder.builder()
                .idCommandeAchat(request.getIdCommandeAchat())
                .product(product)
                .quantiteCommandee(request.getQuantiteCommandee())
                .dateReceptionPrevue(request.getDateReceptionPrevue())
                .statut(request.getStatut() != null ? request.getStatut() : "OPEN")
                .build();

        OpenPurchaseOrder savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrder(String idCommandeAchat, OrderRequest request) {
        OpenPurchaseOrder order = getExistingOrder(idCommandeAchat);

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
            order.setProduct(product);
        }
        if (request.getQuantiteCommandee() != null)
            order.setQuantiteCommandee(request.getQuantiteCommandee());
        if (request.getDateReceptionPrevue() != null)
            order.setDateReceptionPrevue(request.getDateReceptionPrevue());
        if (request.getStatut() != null)
            order.setStatut(request.getStatut());

        OpenPurchaseOrder updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Override
    public void deleteOrder(String idCommandeAchat) {
        OpenPurchaseOrder order = getExistingOrder(idCommandeAchat);
        orderRepository.delete(order);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy == null || sortBy.isBlank()) ? "idCommandeAchat" : sortBy;
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(sortDirection, sortField));
    }

    private OpenPurchaseOrder getExistingOrder(String idCommandeAchat) {
        return orderRepository.findById(idCommandeAchat)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Order not found with id: " + idCommandeAchat));
    }

    private OrderResponse toResponse(OpenPurchaseOrder order) {
        return OrderResponse.builder()
                .idCommandeAchat(order.getIdCommandeAchat())
                .productId(order.getProduct().getId())
                .productSku(order.getProduct().getSku())
                .quantiteCommandee(order.getQuantiteCommandee())
                .dateReceptionPrevue(order.getDateReceptionPrevue())
                .statut(order.getStatut())
                .build();
    }
}