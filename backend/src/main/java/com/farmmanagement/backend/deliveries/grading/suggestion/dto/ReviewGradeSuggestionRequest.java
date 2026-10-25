package com.farmmanagement.backend.deliveries.grading.suggestion.dto;

import jakarta.validation.constraints.Size;

/** Optional note a Manager attaches when approving or rejecting a suggestion. */
public class ReviewGradeSuggestionRequest {

    @Size(max = 1000, message = "Note must be at most 1000 characters")
    private String reviewNotes;

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
