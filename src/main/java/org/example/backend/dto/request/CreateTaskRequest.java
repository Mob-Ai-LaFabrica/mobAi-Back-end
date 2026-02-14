package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.enums.Priority;
import org.example.backend.enums.TransactionType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private String reference;

    private Priority priority;

    private UUID assignedToId;

    private String notes;
}
