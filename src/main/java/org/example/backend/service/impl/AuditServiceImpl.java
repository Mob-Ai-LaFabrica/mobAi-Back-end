package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.AuditLog;
import org.example.backend.entity.User;
import org.example.backend.repository.AuditLogRepository;
import org.example.backend.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(User user, String action, String entityType, UUID entityId,
            String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, entity={}/{}, user={}",
                action, entityType, entityId, user.getUsername());
        return saved;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(User user, String action, String entityType, UUID entityId) {
        return log(user, action, entityType, entityId, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(User user, String action) {
        return log(user, action, null, null, null, null);
    }
}
