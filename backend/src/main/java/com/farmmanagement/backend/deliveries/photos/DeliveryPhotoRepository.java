package com.farmmanagement.backend.deliveries.photos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryPhotoRepository extends JpaRepository<DeliveryPhoto, Long> {

    /**
     * Metadata-only listing for a delivery. A closed interface projection keeps
     * the heavy {@code data} (bytea) column out of the query, so rendering the
     * photo strip never pulls image bytes it doesn't need.
     */
    List<DeliveryPhotoMetadata> findByDeliveryIdOrderByCreatedAtAscIdAsc(Long deliveryId);

    /** Full row (including bytes) for serving or deleting a single photo. */
    Optional<DeliveryPhoto> findByIdAndDeliveryId(Long id, Long deliveryId);

    interface DeliveryPhotoMetadata {
        Long getId();

        String getContentType();

        long getSizeBytes();

        String getOriginalFilename();

        String getSourceUrl();

        LocalDateTime getCreatedAt();

        UploadedBy getUploadedBy();

        interface UploadedBy {
            String getFirstname();

            String getLastname();
        }
    }
}
