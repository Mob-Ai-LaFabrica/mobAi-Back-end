package org.example.backend.dto.request;

import org.example.backend.enums.ChariotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateChariotRequest {

    private ChariotStatus status;

    private Boolean active;
}
