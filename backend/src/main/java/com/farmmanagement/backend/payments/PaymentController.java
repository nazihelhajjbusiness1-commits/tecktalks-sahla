package com.farmmanagement.backend.payments;

import com.farmmanagement.backend.payments.dto.PaymentResponse;
import com.farmmanagement.backend.payments.dto.RecordPaymentRequest;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request,
            Authentication authentication
    ) {
        Long recordedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        PaymentResponse response = paymentService.recordPayment(request, recordedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) PriceRule.Currency currency,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                paymentService.getPayments(farmerId, currency, paymentMethod, status, from, to, pageable));
    }

    @GetMapping("/farmers/{farmerId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<Page<PaymentResponse>> getFarmerPayments(
            @PathVariable Long farmerId,
            @RequestParam(required = false) PriceRule.Currency currency,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                paymentService.getPayments(farmerId, currency, paymentMethod, null, from, to, pageable));
    }
}
