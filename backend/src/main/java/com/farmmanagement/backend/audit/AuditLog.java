package com.farmmanagement.backend.audit;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * DT-88: a single traceable action on a financial/operational record. Captures
 * who did what, when, and (where practical) the old and new values. Written
 * once and never updated or deleted through the application.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Logical record type, e.g. "SETTLEMENT", "PAYMENT". */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /** Action performed, e.g. "CONFIRM", "RECORD". */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Username/email of the actor. */
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected AuditLog() {}

    public AuditLog(String entityType, Long entityId, String action,
                    String previousValue, String newValue, String performedBy, String notes) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.performedBy = performedBy;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getPreviousValue() { return previousValue; }
    public String getNewValue() { return newValue; }
    public String getPerformedBy() { return performedBy; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
