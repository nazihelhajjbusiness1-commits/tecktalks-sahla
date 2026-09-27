package com.farmmanagement.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfirmDeliveryRequest {

    @NotBlank(message = "Confirmed by user identifier is required")
    private String confirmedBy;

    private String notes;

    public ConfirmDeliveryRequest() {}

    public ConfirmDeliveryRequest(String confirmedBy, String notes) {
        this.confirmedBy = confirmedBy;
        this.notes = notes;
    }

    public String getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(String confirmedBy) { this.confirmedBy = confirmedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
