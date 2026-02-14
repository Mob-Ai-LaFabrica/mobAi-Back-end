package org.example.backend.repository;

import org.example.backend.entity.Location;
import org.example.backend.enums.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByCode(String code);

    boolean existsByCode(String code);

    List<Location> findByCodeContainingIgnoreCaseOrZoneContainingIgnoreCase(String code, String zone);

    List<Location> findByWarehouse_Id(UUID warehouseId);

    @Query("SELECT l FROM Location l WHERE " +
            "(:warehouseId IS NULL OR l.warehouse.id = :warehouseId) " +
            "AND (:zone IS NULL OR l.zone = :zone) " +
            "AND (:type IS NULL OR l.type = :type) " +
            "AND (:active IS NULL OR l.active = :active)")
    Page<Location> findWithFilters(
            @Param("warehouseId") UUID warehouseId,
            @Param("zone") String zone,
            @Param("type") LocationType type,
            @Param("active") Boolean active,
            Pageable pageable);
}
