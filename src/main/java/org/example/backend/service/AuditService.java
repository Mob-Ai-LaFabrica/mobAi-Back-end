package org.example.backend.service;

import org.example.backend.entity.AuditLog;
import org.example.backend.entity.User;

import java.util.UUID;

/**
 * Service for creating immutable audit log entries.
 * All significant actions should be logged through this service.
 */
public interface AuditService {

    /**
     * Log an action with entity context.
     */
    AuditLog log(User user, String action, String entityType, UUID entityId,
            String oldValue, String newValue);

    /**
     * Log an action without old/new values.
     */
    AuditLog log(User user, String action, String entityType, UUID entityId);

    /**
     * Log a simple action (e.g., login, logout).
     */
    AuditLog log(User user, String action);
}
