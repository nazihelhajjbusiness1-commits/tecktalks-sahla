package com.farmmanagement.backend.deliveries.grading.suggestion.dto;

import com.farmmanagement.backend.deliveries.grading.suggestion.GradeSuggestion;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.users.User;

import java.time.LocalDateTime;

public class GradeSuggestionResponse {

    private Long id;
    private Long deliveryId;
    private String farmerName;
    private String productName;
    private String currentGradeCode;
    private String currentGradeName;
    private String suggestedGradeCode;
    private String suggestedGradeName;
    private String reason;
    private String status;
    private String suggestedBy;
    private String reviewedBy;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static GradeSuggestionResponse from(GradeSuggestion s) {
        GradeSuggestionResponse r = new GradeSuggestionResponse();
        r.id = s.getId();
        r.deliveryId = s.getDelivery().getId();
        r.farmerName = s.getDelivery().getFarmer() != null ? s.getDelivery().getFarmer().getName() : null;
        r.productName = s.getDelivery().getProduct() != null ? s.getDelivery().getProduct().getName() : null;
        GradeDefinition current = s.getCurrentGrade();
        if (current != null) {
            r.currentGradeCode = current.getGradeCode();
            r.currentGradeName = current.getName();
        }
        GradeDefinition suggested = s.getSuggestedGrade();
        r.suggestedGradeCode = suggested.getGradeCode();
        r.suggestedGradeName = suggested.getName();
        r.reason = s.getReason();
        r.status = s.getStatus().name();
        r.suggestedBy = fullName(s.getSuggestedBy());
        r.reviewedBy = fullName(s.getReviewedBy());
        r.reviewNotes = s.getReviewNotes();
        r.reviewedAt = s.getReviewedAt();
        r.createdAt = s.getCreatedAt();
        return r;
    }

    private static String fullName(User user) {
        if (user == null) {
            return null;
        }
        return ((user.getFirstname() == null ? "" : user.getFirstname()) + " "
                + (user.getLastname() == null ? "" : user.getLastname())).trim();
    }

    public Long getId() {
        return id;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public String getProductName() {
        return productName;
    }

    public String getCurrentGradeCode() {
        return currentGradeCode;
    }

    public String getCurrentGradeName() {
        return currentGradeName;
    }

    public String getSuggestedGradeCode() {
        return suggestedGradeCode;
    }

    public String getSuggestedGradeName() {
        return suggestedGradeName;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getSuggestedBy() {
        return suggestedBy;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
