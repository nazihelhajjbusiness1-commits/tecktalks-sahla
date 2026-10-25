package com.farmmanagement.backend.deliveries.photos;

import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.photos.dto.DeliveryPhotoResponse;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DeliveryPhotoService {

    /** Keep the DB row sane; mirrors the multipart limit in application.yaml. */
    static final long MAX_SIZE_BYTES = 8L * 1024 * 1024;

    /** Browser-renderable image types only — this is inspection evidence, not arbitrary files. */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final DeliveryPhotoRepository deliveryPhotoRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserService userService;

    public DeliveryPhotoService(
            DeliveryPhotoRepository deliveryPhotoRepository,
            DeliveryRepository deliveryRepository,
            UserService userService
    ) {
        this.deliveryPhotoRepository = deliveryPhotoRepository;
        this.deliveryRepository = deliveryRepository;
        this.userService = userService;
    }

    @Transactional
    public DeliveryPhotoResponse upload(Long deliveryId, MultipartFile file, Long uploadedByUserId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (file == null || file.isEmpty()) {
            throw new ValidationException("No file was uploaded");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("Unsupported image type. Allowed: JPEG, PNG, WebP, GIF");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ValidationException("Image is too large (max 8 MB)");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ValidationException("Could not read the uploaded file");
        }

        User uploadedBy = userService.getUserById(uploadedByUserId);

        DeliveryPhoto photo = new DeliveryPhoto();
        photo.setDelivery(delivery);
        photo.setContentType(contentType.toLowerCase());
        photo.setSizeBytes(bytes.length);
        photo.setOriginalFilename(truncate(file.getOriginalFilename(), 255));
        photo.setData(bytes);
        photo.setUploadedBy(uploadedBy);

        deliveryPhotoRepository.save(photo);

        // Re-read as metadata so the response shape matches the list endpoint exactly.
        return list(deliveryId).stream()
                .filter(p -> p.getId().equals(photo.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found after upload"));
    }

    public List<DeliveryPhotoResponse> list(Long deliveryId) {
        if (!deliveryRepository.existsById(deliveryId)) {
            throw new ResourceNotFoundException("Delivery not found");
        }
        return deliveryPhotoRepository.findByDeliveryIdOrderByCreatedAtAscIdAsc(deliveryId)
                .stream()
                .map(DeliveryPhotoResponse::from)
                .toList();
    }

    /** Full row (bytes + content type) for streaming a single photo. */
    public DeliveryPhoto getRaw(Long deliveryId, Long photoId) {
        return deliveryPhotoRepository.findByIdAndDeliveryId(photoId, deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
    }

    @Transactional
    public void delete(Long deliveryId, Long photoId) {
        DeliveryPhoto photo = deliveryPhotoRepository.findByIdAndDeliveryId(photoId, deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
        deliveryPhotoRepository.delete(photo);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
