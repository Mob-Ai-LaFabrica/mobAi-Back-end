package org.example.backend.repository;

import org.example.backend.entity.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductBarcodeRepository extends JpaRepository<ProductBarcode, UUID> {

    Optional<ProductBarcode> findByBarcode(String barcode);
}
