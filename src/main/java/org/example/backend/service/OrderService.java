package org.example.backend.service;

import org.example.backend.dto.request.OrderRequest;
import org.example.backend.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {

    Page<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction);

    OrderResponse getOrderById(String idCommandeAchat);

    Page<OrderResponse> getOrdersByStatut(String statut, int page, int size, String sortBy, String direction);

    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateOrder(String idCommandeAchat, OrderRequest request);

    void deleteOrder(String idCommandeAchat);
}