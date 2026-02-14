package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.enums.LocationType;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationCreateRequest {

    @NotBlank(message = "Location code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouseId;

    @Size(max = 50, message = "Zone must not exceed 50 characters")
    private String zone;

    @NotNull(message = "Location type is required")
    private LocationType type;

    private Double positionX;

    private Double positionY;

    @Builder.Default
    private Double positionZ = 0.0;
}
