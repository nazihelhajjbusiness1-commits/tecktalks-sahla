package com.farmmanagement.backend.audit;

import com.farmmanagement.backend.audit.dto.AuditLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DT-88: read-only audit history. Only ADMIN/MANAGER may view it, and there is
 * deliberately no write/delete endpoint — audit rows cannot be tampered with
 * through the API.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AuditLogResponse>> history(
            @RequestParam String entityType,
            @RequestParam(required = false) Long entityId
    ) {
        return ResponseEntity.ok(auditService.history(entityType, entityId));
    }
}
