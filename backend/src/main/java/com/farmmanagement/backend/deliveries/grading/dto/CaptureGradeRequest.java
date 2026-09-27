package com.farmmanagement.backend.deliveries.grading.dto;

import com.farmmanagement.backend.deliveries.grading.Grade;
import jakarta.validation.constraints.NotNull;

public class CaptureGradeRequest {

    @NotNull(message = "Grade is required")
    private Grade grade;

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}
