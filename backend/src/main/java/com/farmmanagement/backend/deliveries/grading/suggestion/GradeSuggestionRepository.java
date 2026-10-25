package com.farmmanagement.backend.deliveries.grading.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeSuggestionRepository extends JpaRepository<GradeSuggestion, Long> {

    List<GradeSuggestion> findByDeliveryIdOrderByCreatedAtDesc(Long deliveryId);

    List<GradeSuggestion> findByStatusOrderByCreatedAtAsc(GradeSuggestionStatus status);

    boolean existsByDeliveryIdAndStatus(Long deliveryId, GradeSuggestionStatus status);

    long countByStatus(GradeSuggestionStatus status);
}
