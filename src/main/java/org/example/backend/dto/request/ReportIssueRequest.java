package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIssueRequest {

    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    private Integer lineNumber;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "Issue type is required")
    private String issueType;

    private Integer expectedQuantity;

    private Integer actualQuantity;

    private String notes;
}