package org.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.enums.LocationType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLocationRequest {

    private String zone;

    private LocationType type;

    private Double positionX;

    private Double positionY;

    private Boolean active;
}
