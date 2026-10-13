package com.farmmanagement.backend.audit.dto;

import com.farmmanagement.backend.audit.AuditLog;

import java.time.OffsetDateTime;

/** UI-friendly audit entry (DT-88). */
public record AuditLogResponse(
        Long id,
        String entityType,
        Long entityId,
        String action,
        String previousValue,
        String newValue,
        String performedBy,
        String notes,
        OffsetDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getPreviousValue(),
                log.getNewValue(),
                log.getPerformedBy(),
                log.getNotes(),
                log.getCreatedAt());
    }
}
