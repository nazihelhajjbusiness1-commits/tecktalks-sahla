package com.farmmanagement.backend.deliveries;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "delivery_audit_logs")
public class DeliveryAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @NotNull
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @NotNull
    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;

    @NotNull
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "notes")
    private String notes;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public DeliveryAuditLog() {}

    public DeliveryAuditLog(Delivery delivery, String action, String previousStatus, String newStatus, String performedBy, String notes) {
        this.delivery = delivery;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.performedBy = performedBy;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Delivery getDelivery() { return delivery; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getPerformedBy() { return performedBy; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
