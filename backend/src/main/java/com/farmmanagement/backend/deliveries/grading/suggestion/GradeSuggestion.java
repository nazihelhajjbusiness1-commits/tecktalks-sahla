package com.farmmanagement.backend.deliveries.grading.suggestion;

import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.users.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A proposed grade change for a delivery, raised by an inspector reviewing the
 * produce (and its photos). Starts {@code PENDING}; a Manager either approves it
 * (which overrides the delivery's grade) or rejects it.
 */
@Entity
@Table(name = "grade_suggestions")
public class GradeSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    /** The grade on the delivery when the suggestion was raised (context). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_grade_id")
    private GradeDefinition currentGrade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suggested_grade_id", nullable = false)
    private GradeDefinition suggestedGrade;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GradeSuggestionStatus status = GradeSuggestionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suggested_by_user_id", nullable = false)
    private User suggestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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

    public GradeDefinition getCurrentGrade() {
        return currentGrade;
    }

    public void setCurrentGrade(GradeDefinition currentGrade) {
        this.currentGrade = currentGrade;
    }

    public GradeDefinition getSuggestedGrade() {
        return suggestedGrade;
    }

    public void setSuggestedGrade(GradeDefinition suggestedGrade) {
        this.suggestedGrade = suggestedGrade;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public GradeSuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(GradeSuggestionStatus status) {
        this.status = status;
    }

    public User getSuggestedBy() {
        return suggestedBy;
    }

    public void setSuggestedBy(User suggestedBy) {
        this.suggestedBy = suggestedBy;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
