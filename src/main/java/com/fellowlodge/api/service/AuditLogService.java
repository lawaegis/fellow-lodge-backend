package com.fellowlodge.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fellowlodge.api.entity.AuditLog;
import com.fellowlodge.api.repository.AuditLogRepository;
import com.fellowlodge.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists audit log entries. Runs asynchronously and in its own transaction so a
 * logging failure never breaks the business operation being audited.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Captures the authenticated context on the calling thread, then persists
     * the audit entry asynchronously in its own transaction.
     */
    public void record(String action, String tableName, String recordId, Object oldValues, Object newValues) {
        java.util.UUID userId = SecurityUtils.currentUserId();
        String username = SecurityUtils.currentUsername();
        persistAsync(action, tableName, recordId, oldValues, newValues, userId, username);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAsync(String action, String tableName, String recordId,
                             Object oldValues, Object newValues,
                             java.util.UUID userId, String username) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValues(toJson(oldValues));
        log.setNewValues(toJson(newValues));
        auditLogRepository.save(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
