package com.farmmanagement.backend.deliveries.photos;

import com.farmmanagement.backend.deliveries.photos.dto.DeliveryPhotoResponse;
import com.farmmanagement.backend.users.UserService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

/**
 * Produce photos for a delivery (inspection evidence). Receiving staff attach
 * them at intake and the Quality Inspector can add evidence while grading;
 * everyone with delivery access can view them inline in the web app.
 */
@RestController
@RequestMapping("/api/deliveries/{deliveryId}/photos")
public class DeliveryPhotoController {

    private final DeliveryPhotoService deliveryPhotoService;
    private final UserService userService;

    public DeliveryPhotoController(DeliveryPhotoService deliveryPhotoService, UserService userService) {
        this.deliveryPhotoService = deliveryPhotoService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<DeliveryPhotoResponse> upload(
            @PathVariable Long deliveryId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long uploadedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        DeliveryPhotoResponse response = deliveryPhotoService.upload(deliveryId, file, uploadedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<List<DeliveryPhotoResponse>> list(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(deliveryPhotoService.list(deliveryId));
    }

    @GetMapping("/{photoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<byte[]> view(
            @PathVariable Long deliveryId,
            @PathVariable Long photoId
    ) {
        DeliveryPhoto photo = deliveryPhotoService.getRaw(deliveryId, photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(photo.getOriginalFilename() == null ? "photo" : photo.getOriginalFilename())
                                .build()
                                .toString()
                )
                .body(photo.getData());
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<Void> delete(
            @PathVariable Long deliveryId,
            @PathVariable Long photoId
    ) {
        deliveryPhotoService.delete(deliveryId, photoId);
        return ResponseEntity.noContent().build();
    }
}
