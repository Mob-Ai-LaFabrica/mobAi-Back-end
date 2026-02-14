package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChariotRequest {

    @NotBlank(message = "Chariot code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;
}
