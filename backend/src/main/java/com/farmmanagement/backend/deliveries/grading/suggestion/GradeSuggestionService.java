package com.farmmanagement.backend.deliveries.grading.suggestion;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryAuditLog;
import com.farmmanagement.backend.deliveries.DeliveryAuditLogRepository;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.grading.suggestion.dto.CreateGradeSuggestionRequest;
import com.farmmanagement.backend.deliveries.grading.suggestion.dto.GradeSuggestionResponse;
import com.farmmanagement.backend.deliveries.grading.suggestion.dto.ReviewGradeSuggestionRequest;
import com.farmmanagement.backend.deliveries.settlement.FarmerSettlementRepository;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.grading.GradeDefinitionRepository;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.pricing.PriceRuleRepository;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GradeSuggestionService {

    private static final String REJECT_CODE = "REJECT";

    private final GradeSuggestionRepository suggestionRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryGradeRepository deliveryGradeRepository;
    private final GradeDefinitionRepository gradeDefinitionRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final FarmerSettlementRepository farmerSettlementRepository;
    private final DeliveryAuditLogRepository auditLogRepository;
    private final UserService userService;

    public GradeSuggestionService(
            GradeSuggestionRepository suggestionRepository,
            DeliveryRepository deliveryRepository,
            DeliveryGradeRepository deliveryGradeRepository,
            GradeDefinitionRepository gradeDefinitionRepository,
            PriceRuleRepository priceRuleRepository,
            FarmerSettlementRepository farmerSettlementRepository,
            DeliveryAuditLogRepository auditLogRepository,
            UserService userService
    ) {
        this.suggestionRepository = suggestionRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryGradeRepository = deliveryGradeRepository;
        this.gradeDefinitionRepository = gradeDefinitionRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.farmerSettlementRepository = farmerSettlementRepository;
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
    }

    @Transactional
    public GradeSuggestionResponse create(Long deliveryId, CreateGradeSuggestionRequest request, Long suggestedByUserId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        DeliveryGrade existingGrade = deliveryGradeRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ValidationException(
                        "This delivery has not been graded yet, so there is no grade to review"));

        Long productId = delivery.getProduct().getId();
        GradeDefinition suggested = gradeDefinitionRepository
                .findByProductIdAndGradeCode(productId, request.getSuggestedGrade())
                .orElseThrow(() -> new ValidationException(
                        "No grade '" + request.getSuggestedGrade() + "' is configured for this product"));

        GradeDefinition current = existingGrade.getGradeDefinition();
        if (current != null && current.getId().equals(suggested.getId())) {
            throw new ValidationException("The suggested grade is the same as the current grade");
        }

        if (suggestionRepository.existsByDeliveryIdAndStatus(deliveryId, GradeSuggestionStatus.PENDING)) {
            throw new ConflictException("A pending grade suggestion already exists for this delivery");
        }

        User suggestedBy = userService.getUserById(suggestedByUserId);

        GradeSuggestion suggestion = new GradeSuggestion();
        suggestion.setDelivery(delivery);
        suggestion.setCurrentGrade(current);
        suggestion.setSuggestedGrade(suggested);
        suggestion.setReason(request.getReason());
        suggestion.setStatus(GradeSuggestionStatus.PENDING);
        suggestion.setSuggestedBy(suggestedBy);

        return GradeSuggestionResponse.from(suggestionRepository.save(suggestion));
    }

    public List<GradeSuggestionResponse> listForDelivery(Long deliveryId) {
        return suggestionRepository.findByDeliveryIdOrderByCreatedAtDesc(deliveryId)
                .stream()
                .map(GradeSuggestionResponse::from)
                .toList();
    }

    public List<GradeSuggestionResponse> listPending() {
        return suggestionRepository.findByStatusOrderByCreatedAtAsc(GradeSuggestionStatus.PENDING)
                .stream()
                .map(GradeSuggestionResponse::from)
                .toList();
    }

    public long pendingCount() {
        return suggestionRepository.countByStatus(GradeSuggestionStatus.PENDING);
    }

    /** Manager approves: the delivery's grade is overridden to the suggested grade. */
    @Transactional
    public GradeSuggestionResponse approve(Long suggestionId, ReviewGradeSuggestionRequest request, Long reviewerUserId) {
        GradeSuggestion suggestion = loadPending(suggestionId);
        Delivery delivery = suggestion.getDelivery();

        // Protect the money trail: a settled delivery can't be silently re-priced.
        if (farmerSettlementRepository.existsByDeliveryId(delivery.getId())) {
            throw new ConflictException(
                    "This delivery already has a settlement; reverse the settlement before overriding the grade");
        }

        DeliveryGrade grade = deliveryGradeRepository.findByDeliveryId(delivery.getId())
                .orElseThrow(() -> new ConflictException("The delivery no longer has a grade to override"));

        GradeDefinition suggested = suggestion.getSuggestedGrade();
        GradeDefinition current = grade.getGradeDefinition();

        // Reject transitions would require inventory add/remove reversal (not yet
        // supported), so keep overrides between real grades only.
        if (isReject(suggested) || isReject(current)) {
            throw new ConflictException(
                    "Overrides to or from Reject require inventory reversal, which isn't supported yet");
        }

        BigDecimal totalPrice = computePrice(delivery, suggested);

        DeliveryStatus previousStatus = delivery.getStatus();

        grade.setGradeDefinition(suggested);
        grade.setNotes(appendOverrideNote(grade.getNotes(), suggestion));
        deliveryGradeRepository.save(grade);

        delivery.setStatus(DeliveryStatus.CONFIRMED);
        delivery.setTotalPrice(totalPrice);
        delivery.setTotalPriceCurrency(activeCurrency(delivery, suggested));
        deliveryRepository.save(delivery);

        User reviewer = userService.getUserById(reviewerUserId);
        suggestion.setStatus(GradeSuggestionStatus.APPROVED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());
        suggestion.setReviewNotes(request == null ? null : request.getReviewNotes());

        auditLogRepository.save(new DeliveryAuditLog(
                delivery,
                "GRADE_OVERRIDDEN",
                previousStatus.name(),
                delivery.getStatus().name(),
                fullName(reviewer),
                "Grade changed "
                        + (current == null ? "?" : current.getGradeCode()) + " → " + suggested.getGradeCode()
                        + " via suggestion #" + suggestion.getId() + ". " + suggestion.getReason()
        ));

        return GradeSuggestionResponse.from(suggestionRepository.save(suggestion));
    }

    @Transactional
    public GradeSuggestionResponse reject(Long suggestionId, ReviewGradeSuggestionRequest request, Long reviewerUserId) {
        GradeSuggestion suggestion = loadPending(suggestionId);
        User reviewer = userService.getUserById(reviewerUserId);
        suggestion.setStatus(GradeSuggestionStatus.REJECTED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());
        suggestion.setReviewNotes(request == null ? null : request.getReviewNotes());
        return GradeSuggestionResponse.from(suggestionRepository.save(suggestion));
    }

    private GradeSuggestion loadPending(Long suggestionId) {
        GradeSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade suggestion not found"));
        if (suggestion.getStatus() != GradeSuggestionStatus.PENDING) {
            throw new ConflictException("This suggestion has already been " + suggestion.getStatus().name().toLowerCase());
        }
        return suggestion;
    }

    private BigDecimal computePrice(Delivery delivery, GradeDefinition grade) {
        return activeRule(delivery, grade).getAmount().multiply(delivery.getQuantity());
    }

    private PriceRule.Currency activeCurrency(Delivery delivery, GradeDefinition grade) {
        return activeRule(delivery, grade).getCurrency();
    }

    private PriceRule activeRule(Delivery delivery, GradeDefinition grade) {
        List<PriceRule> rules = priceRuleRepository.findActivePriceRules(
                delivery.getProduct().getId(), grade.getId(), OffsetDateTime.now());
        if (rules.isEmpty()) {
            throw new ConflictException(
                    "No active price rule configured for grade " + grade.getGradeCode());
        }
        if (rules.size() > 1) {
            throw new ConflictException(
                    "Multiple active price rules found for grade " + grade.getGradeCode() + "; cannot determine the price");
        }
        return rules.get(0);
    }

    private static boolean isReject(GradeDefinition grade) {
        return grade != null && REJECT_CODE.equalsIgnoreCase(grade.getGradeCode());
    }

    private static String appendOverrideNote(String existing, GradeSuggestion suggestion) {
        String override = "Grade overridden via suggestion #" + suggestion.getId() + ": " + suggestion.getReason();
        if (existing == null || existing.isBlank()) {
            return override;
        }
        return existing + " | " + override;
    }

    private static String fullName(User user) {
        if (user == null) {
            return "system";
        }
        return ((user.getFirstname() == null ? "" : user.getFirstname()) + " "
                + (user.getLastname() == null ? "" : user.getLastname())).trim();
    }
}
