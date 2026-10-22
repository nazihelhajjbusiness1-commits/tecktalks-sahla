package com.farmmanagement.backend.pricing;

import com.farmmanagement.backend.pricing.dto.PriceRuleRequest;
import com.farmmanagement.backend.pricing.dto.PriceRuleResponse;
import com.farmmanagement.backend.pricing.dto.PriceRuleUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PriceRuleController {

    private final PriceRuleService priceRuleService;

    @GetMapping("/products/{productId}/prices")
    public ResponseEntity<List<PriceRuleResponse>> getPricesForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(priceRuleService.getPricesForProduct(productId));
    }

    @PostMapping("/products/{productId}/prices")
    public ResponseEntity<PriceRuleResponse> createPrice(
            @PathVariable Long productId,
            @Valid @RequestBody PriceRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(priceRuleService.createPrice(productId, request));
    }

    @PutMapping("/prices/{priceId}")
    public ResponseEntity<PriceRuleResponse> updatePrice(
            @PathVariable Long priceId,
            @Valid @RequestBody PriceRuleUpdateRequest request) {
        return ResponseEntity.ok(priceRuleService.updatePrice(priceId, request));
    }
}
