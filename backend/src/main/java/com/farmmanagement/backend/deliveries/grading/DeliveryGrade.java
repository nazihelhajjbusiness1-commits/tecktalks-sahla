package com.farmmanagement.backend.deliveries.grading;

import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.users.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_grades")
public class DeliveryGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false, unique = true)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private GradeDefinition gradeDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "graded_by_user_id", nullable = false)
    private User gradedBy;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public GradeDefinition getGradeDefinition() {
        return gradeDefinition;
    }

    public void setGradeDefinition(GradeDefinition gradeDefinition) {
        this.gradeDefinition = gradeDefinition;
    }

    public User getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(User gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
