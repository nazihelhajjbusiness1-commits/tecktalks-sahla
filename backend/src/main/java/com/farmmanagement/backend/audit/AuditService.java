package com.farmmanagement.backend.audit;

import com.farmmanagement.backend.audit.dto.AuditLogResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DT-88: records and reads audit entries. {@link #record} is called inside the
 * caller's transaction so an action and its audit row commit together.
 */
@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String entityType, Long entityId, String action,
                       String previousValue, String newValue, String performedBy, String notes) {
        repository.save(new AuditLog(
                entityType, entityId, action, previousValue, newValue,
                performedBy != null ? performedBy : "system", notes));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> history(String entityType, Long entityId) {
        List<AuditLog> logs = (entityId != null)
                ? repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                : repository.findByEntityTypeOrderByCreatedAtDesc(entityType);
        return logs.stream().map(AuditLogResponse::from).toList();
    }
}
