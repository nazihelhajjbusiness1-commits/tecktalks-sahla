package com.farmmanagement.backend.deliveries;

import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.dto.CreateDeliveryRequest;
import com.farmmanagement.backend.deliveries.dto.DeliveryResponse;
import com.farmmanagement.backend.deliveries.dto.UpdateDeliveryRequest;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.products.Product;
import com.farmmanagement.backend.products.ProductRepository;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final FarmerRepository farmerRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            FarmerRepository farmerRepository,
            ProductRepository productRepository,
            UserService userService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.farmerRepository = farmerRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request, Long createdByUserId) {

        Farmer farmer = farmerRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User createdBy = userService.getUserById(createdByUserId);

        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(request.getQuantity());
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(request.getDeliveryDate());
        delivery.setNotes(request.getNotes());
        delivery.setCreatedBy(createdBy);
        delivery.setStatus(DeliveryStatus.PENDING);

        Delivery saved = deliveryRepository.save(delivery);

        return toResponse(saved);
    }

    public DeliveryResponse getDelivery(Long id) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        return toResponse(delivery);
    }

    public Page<DeliveryResponse> getDeliveries(
            Long farmerId,
            Long productId,
            DeliveryStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("'from' date must not be after 'to' date");
        }

        Specification<Delivery> spec = buildSearchSpecification(farmerId, productId, status, from, to);

        return deliveryRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private Specification<Delivery> buildSearchSpecification(
            Long farmerId,
            Long productId,
            DeliveryStatus status,
            LocalDate from,
            LocalDate to
    ) {
        return (root, query, cb) -> {

            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("farmer", JoinType.LEFT);
                root.fetch("product", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (farmerId != null) {
                predicates.add(cb.equal(root.get("farmer").get("id"), farmerId));
            }
            if (productId != null) {
                predicates.add(cb.equal(root.get("product").get("id"), productId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deliveryDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("deliveryDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public DeliveryResponse updateDelivery(Long id, UpdateDeliveryRequest request) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (request.getQuantity() != null) {
            delivery.setQuantity(request.getQuantity());
        }
        if (request.getDeliveryDate() != null) {
            delivery.setDeliveryDate(request.getDeliveryDate());
        }
        if (request.getNotes() != null) {
            delivery.setNotes(request.getNotes());
        }

        Delivery updated = deliveryRepository.save(delivery);

        return toResponse(updated);
    }

    private DeliveryResponse toResponse(Delivery delivery) {

        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setFarmerCode(delivery.getFarmer().getFarmerCode());
        response.setFarmerName(delivery.getFarmer().getName());
        response.setProductName(delivery.getProduct().getName());
        response.setQuantity(delivery.getQuantity());
        response.setUnit(delivery.getUnit());
        response.setDeliveryDate(delivery.getDeliveryDate());
        response.setCreatedBy(formatUserName(delivery.getCreatedBy()));
        response.setNotes(delivery.getNotes());
        response.setStatus(delivery.getStatus());
        response.setTotalPrice(delivery.getTotalPrice());
        response.setTotalPriceCurrency(delivery.getTotalPriceCurrency());
        response.setCreatedAt(delivery.getCreatedAt());
        response.setUpdatedAt(delivery.getUpdatedAt());

        return response;
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstname() + " " + user.getLastname()).trim();
    }
}
