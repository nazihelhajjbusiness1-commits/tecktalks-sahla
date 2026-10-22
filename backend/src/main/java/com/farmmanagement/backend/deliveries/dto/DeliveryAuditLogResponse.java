package com.farmmanagement.backend.deliveries.dto;

import com.farmmanagement.backend.deliveries.DeliveryAuditLog;

import java.time.OffsetDateTime;

/**
 * Flat, serialization-safe view of a delivery audit record (DT-57).
 * Avoids exposing the lazy Delivery association on the JPA entity.
 */
public class DeliveryAuditLogResponse {

    private Long id;
    private String action;
    private String previousStatus;
    private String newStatus;
    private String performedBy;
    private String notes;
    private OffsetDateTime createdAt;

    public static DeliveryAuditLogResponse from(DeliveryAuditLog log) {
        DeliveryAuditLogResponse r = new DeliveryAuditLogResponse();
        r.id = log.getId();
        r.action = log.getAction();
        r.previousStatus = log.getPreviousStatus();
        r.newStatus = log.getNewStatus();
        r.performedBy = log.getPerformedBy();
        r.notes = log.getNotes();
        r.createdAt = log.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getPerformedBy() { return performedBy; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
