package com.farmmanagement.backend.deliveries.grading.suggestion;

import com.farmmanagement.backend.deliveries.grading.suggestion.dto.CreateGradeSuggestionRequest;
import com.farmmanagement.backend.deliveries.grading.suggestion.dto.GradeSuggestionResponse;
import com.farmmanagement.backend.deliveries.grading.suggestion.dto.ReviewGradeSuggestionRequest;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Grade suggestions (second-opinion re-grade requests) and the Manager's
 * approval queue. Inspectors raise suggestions on a delivery; Managers approve
 * (overriding the grade) or reject them.
 */
@RestController
@RequestMapping("/api")
public class GradeSuggestionController {

    private final GradeSuggestionService suggestionService;
    private final UserService userService;

    public GradeSuggestionController(GradeSuggestionService suggestionService, UserService userService) {
        this.suggestionService = suggestionService;
        this.userService = userService;
    }

    @PostMapping("/deliveries/{deliveryId}/grade-suggestions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INSPECTOR')")
    public ResponseEntity<GradeSuggestionResponse> create(
            @PathVariable Long deliveryId,
            @Valid @RequestBody CreateGradeSuggestionRequest request,
            Authentication authentication
    ) {
        Long userId = userService.getUserByEmail(authentication.getName()).getId();
        GradeSuggestionResponse response = suggestionService.create(deliveryId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/deliveries/{deliveryId}/grade-suggestions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<List<GradeSuggestionResponse>> listForDelivery(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(suggestionService.listForDelivery(deliveryId));
    }

    @GetMapping("/grade-suggestions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<GradeSuggestionResponse>> pendingQueue() {
        return ResponseEntity.ok(suggestionService.listPending());
    }

    @GetMapping("/grade-suggestions/pending-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> pendingCount() {
        return ResponseEntity.ok(Map.of("count", suggestionService.pendingCount()));
    }

    @PostMapping("/grade-suggestions/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GradeSuggestionResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewGradeSuggestionRequest request,
            Authentication authentication
    ) {
        Long userId = userService.getUserByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(suggestionService.approve(id, request, userId));
    }

    @PostMapping("/grade-suggestions/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GradeSuggestionResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReviewGradeSuggestionRequest request,
            Authentication authentication
    ) {
        Long userId = userService.getUserByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(suggestionService.reject(id, request, userId));
    }
}
