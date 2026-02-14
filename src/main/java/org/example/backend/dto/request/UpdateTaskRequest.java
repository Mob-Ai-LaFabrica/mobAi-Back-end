package org.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.enums.Priority;
import org.example.backend.enums.TransactionStatus;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {

    private UUID assignedToId;

    private Priority priority;

    private TransactionStatus status;

    private String notes;
}
