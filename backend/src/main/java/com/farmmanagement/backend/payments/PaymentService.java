package com.farmmanagement.backend.payments;

import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.ledger.LedgerService;
import com.farmmanagement.backend.payments.dto.PaymentResponse;
import com.farmmanagement.backend.payments.dto.RecordPaymentRequest;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records full/partial farmer payments (DT-72). A payment and its PAYMENT_DEBIT
 * ledger entry are written in one transaction; the backend validates currency
 * and outstanding balance so a payment can never push a balance below zero.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final FarmerPaymentRepository paymentRepository;
    private final LedgerService ledgerService;
    private final FarmerRepository farmerRepository;
    private final UserService userService;

    public PaymentService(
            FarmerPaymentRepository paymentRepository,
            LedgerService ledgerService,
            FarmerRepository farmerRepository,
            UserService userService
    ) {
        this.paymentRepository = paymentRepository;
        this.ledgerService = ledgerService;
        this.farmerRepository = farmerRepository;
        this.userService = userService;
    }

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request, Long recordedByUserId) {

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new ValidationException("Payment amount must be greater than zero");
        }

        if (!farmerRepository.existsById(request.getFarmerId())) {
            throw new ResourceNotFoundException("Farmer not found");
        }

        PriceRule.Currency currency = request.getCurrency();
        BigDecimal outstanding = ledgerService.getOutstanding(request.getFarmerId(), currency);

        if (outstanding.signum() <= 0) {
            throw new ValidationException(
                    "Farmer has no outstanding balance in " + currency);
        }
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new ValidationException(
                    "Payment exceeds the outstanding balance of " + outstanding + " " + currency);
        }

        FarmerPayment payment = new FarmerPayment();
        payment.setFarmerId(request.getFarmerId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(currency);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setPaidAt(request.getPaidAt() != null ? request.getPaidAt() : LocalDateTime.now());
        payment.setRecordedByUserId(recordedByUserId);
        payment.setStatus(PaymentStatus.RECORDED);

        FarmerPayment saved = paymentRepository.save(payment);

        ledgerService.postPaymentDebit(
                saved.getFarmerId(),
                saved.getAmount(),
                currency,
                saved.getId(),
                saved.getPaidAt(),
                recordedByUserId
        );

        BigDecimal outstandingAfter = outstanding.subtract(saved.getAmount());
        return PaymentResponse.from(saved, userName(recordedByUserId, new HashMap<>()), outstandingAfter);
    }

    public Page<PaymentResponse> getPayments(
            Long farmerId,
            PriceRule.Currency currency,
            PaymentMethod method,
            PaymentStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        Specification<FarmerPayment> spec = buildSpec(farmerId, currency, method, status, from, to);
        Map<Long, String> nameCache = new HashMap<>();
        return paymentRepository.findAll(spec, pageable)
                .map(p -> PaymentResponse.from(p, userName(p.getRecordedByUserId(), nameCache), null));
    }

    private Specification<FarmerPayment> buildSpec(
            Long farmerId,
            PriceRule.Currency currency,
            PaymentMethod method,
            PaymentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (farmerId != null) predicates.add(cb.equal(root.get("farmerId"), farmerId));
            if (currency != null) predicates.add(cb.equal(root.get("currency"), currency));
            if (method != null) predicates.add(cb.equal(root.get("paymentMethod"), method));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("paidAt"), from.atStartOfDay()));
            if (to != null) predicates.add(cb.lessThan(root.get("paidAt"), to.plusDays(1).atStartOfDay()));
            if (query != null) query.orderBy(cb.desc(root.get("paidAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String userName(Long userId, Map<Long, String> cache) {
        if (userId == null) return null;
        return cache.computeIfAbsent(userId, id -> {
            User user = userService.getUserById(id);
            return user == null ? null : (user.getFirstname() + " " + user.getLastname()).trim();
        });
    }
}
