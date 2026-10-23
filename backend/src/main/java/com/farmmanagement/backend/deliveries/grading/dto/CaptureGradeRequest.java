package com.farmmanagement.backend.deliveries.grading.dto;

import com.farmmanagement.backend.deliveries.grading.Grade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CaptureGradeRequest {

    @NotNull(message = "Grade is required")
    private Grade grade;

    /** Optional inspector note / reason (e.g. observed defects). */
    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
