package org.example.backend.repository;

import org.example.backend.entity.OpenPurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenPurchaseOrderRepository extends JpaRepository<OpenPurchaseOrder, String> {

    List<OpenPurchaseOrder> findByStatut(String statut);

    Page<OpenPurchaseOrder> findByStatut(String statut, Pageable pageable);

    List<OpenPurchaseOrder> findByProduct_Id(java.util.UUID productId);
}
