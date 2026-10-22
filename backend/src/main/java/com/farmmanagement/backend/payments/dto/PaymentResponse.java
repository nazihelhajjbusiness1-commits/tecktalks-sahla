package com.farmmanagement.backend.payments.dto;

import com.farmmanagement.backend.payments.FarmerPayment;
import com.farmmanagement.backend.payments.PaymentMethod;
import com.farmmanagement.backend.payments.PaymentStatus;
import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long farmerId;
    private BigDecimal amount;
    private PriceRule.Currency currency;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String notes;
    private LocalDateTime paidAt;
    private String recordedBy;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    /** Outstanding balance in the payment currency immediately after this payment. */
    private BigDecimal outstandingAfter;

    public static PaymentResponse from(FarmerPayment p, String recordedBy, BigDecimal outstandingAfter) {
        PaymentResponse r = new PaymentResponse();
        r.id = p.getId();
        r.farmerId = p.getFarmerId();
        r.amount = p.getAmount();
        r.currency = p.getCurrency();
        r.paymentMethod = p.getPaymentMethod();
        r.referenceNumber = p.getReferenceNumber();
        r.notes = p.getNotes();
        r.paidAt = p.getPaidAt();
        r.recordedBy = recordedBy;
        r.status = p.getStatus();
        r.createdAt = p.getCreatedAt();
        r.outstandingAfter = outstandingAfter;
        return r;
    }

    public Long getId() { return id; }
    public Long getFarmerId() { return farmerId; }
    public BigDecimal getAmount() { return amount; }
    public PriceRule.Currency getCurrency() { return currency; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getReferenceNumber() { return referenceNumber; }
    public String getNotes() { return notes; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getRecordedBy() { return recordedBy; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getOutstandingAfter() { return outstandingAfter; }
}
