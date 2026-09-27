package com.farmmanagement.backend.deliveries.grading;

import com.farmmanagement.backend.deliveries.grading.dto.CaptureGradeRequest;
import com.farmmanagement.backend.deliveries.grading.dto.DeliveryGradeResponse;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryGradeController {

    private final DeliveryGradeService deliveryGradeService;
    private final UserService userService;

    public DeliveryGradeController(DeliveryGradeService deliveryGradeService, UserService userService) {
        this.deliveryGradeService = deliveryGradeService;
        this.userService = userService;
    }

    @PostMapping("/{deliveryId}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INSPECTOR')")
    public ResponseEntity<DeliveryGradeResponse> gradeDelivery(
            @PathVariable Long deliveryId,
            @Valid @RequestBody CaptureGradeRequest request,
            Authentication authentication
    ) {
        Long gradedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        DeliveryGradeResponse response = deliveryGradeService.gradeDelivery(deliveryId, request, gradedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
