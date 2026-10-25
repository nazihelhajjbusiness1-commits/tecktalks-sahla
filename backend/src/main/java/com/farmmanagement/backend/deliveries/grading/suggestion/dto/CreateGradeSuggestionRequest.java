package com.farmmanagement.backend.deliveries.grading.suggestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateGradeSuggestionRequest {

    /** Grade code the inspector proposes instead (e.g. "GRADE_B"). */
    @NotBlank(message = "A suggested grade is required")
    private String suggestedGrade;

    @NotBlank(message = "A reason is required")
    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;

    public String getSuggestedGrade() {
        return suggestedGrade;
    }

    public void setSuggestedGrade(String suggestedGrade) {
        this.suggestedGrade = suggestedGrade;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
