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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Bytes of a photo plus the content type to serve it with. */
    public record PhotoBytes(byte[] data, String contentType) {
    }

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

    /**
     * Bytes + content type for streaming a single photo. Inline uploads return
     * their stored bytes directly; external (seeded) photos are fetched from
     * their {@code source_url} server-side so they stay behind the same auth and
     * avoid browser cross-origin limits.
     */
    public PhotoBytes loadBytes(Long deliveryId, Long photoId) {
        DeliveryPhoto photo = deliveryPhotoRepository.findByIdAndDeliveryId(photoId, deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (photo.getData() != null) {
            return new PhotoBytes(photo.getData(), photo.getContentType());
        }
        return fetchExternal(photo);
    }

    private PhotoBytes fetchExternal(DeliveryPhoto photo) {
        String url = photo.getSourceUrl();
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new ResourceNotFoundException("Photo has no retrievable image");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() / 100 != 2 || response.body() == null || response.body().length == 0) {
                throw new ResourceNotFoundException("Could not retrieve the photo from its source");
            }
            String contentType = response.headers().firstValue("content-type")
                    .filter(ct -> ct.startsWith("image/"))
                    .orElse(photo.getContentType());
            return new PhotoBytes(response.body(), contentType);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Could not retrieve the photo from its source");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResourceNotFoundException("Could not retrieve the photo from its source");
        }
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
