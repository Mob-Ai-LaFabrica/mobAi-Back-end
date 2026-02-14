package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.enums.Priority;
import org.example.backend.enums.TransactionType;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreateTaskRequest {

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    private UUID assignedToId;

    private UUID chariotId;

    private String notes;

    @NotNull(message = "Task lines are required")
    private List<TaskLine> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskLine {
        @NotNull(message = "Product ID is required")
        private UUID productId;
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        private UUID sourceLocationId;
        private UUID destinationLocationId;
    }
}
