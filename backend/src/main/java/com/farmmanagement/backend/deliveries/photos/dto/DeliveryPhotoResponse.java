package com.farmmanagement.backend.deliveries.photos.dto;

import com.farmmanagement.backend.deliveries.photos.DeliveryPhotoRepository.DeliveryPhotoMetadata;

import java.time.LocalDateTime;

/**
 * Metadata for one delivery photo. The bytes are fetched separately from
 * {@code GET /api/deliveries/{id}/photos/{photoId}} so the list stays light.
 */
public class DeliveryPhotoResponse {

    private Long id;
    private String contentType;
    private long sizeBytes;
    private String originalFilename;
    private String uploadedBy;
    private LocalDateTime createdAt;

    public static DeliveryPhotoResponse from(DeliveryPhotoMetadata meta) {
        DeliveryPhotoResponse response = new DeliveryPhotoResponse();
        response.id = meta.getId();
        response.contentType = meta.getContentType();
        response.sizeBytes = meta.getSizeBytes();
        response.originalFilename = meta.getOriginalFilename();
        response.createdAt = meta.getCreatedAt();
        DeliveryPhotoMetadata.UploadedBy uploader = meta.getUploadedBy();
        if (uploader != null) {
            response.uploadedBy = (safe(uploader.getFirstname()) + " " + safe(uploader.getLastname())).trim();
        }
        return response;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public Long getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
