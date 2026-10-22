package com.farmmanagement.backend.farmers;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.farmers.dto.CreateFarmerRequest;
import com.farmmanagement.backend.farmers.dto.FarmerResponse;
import com.farmmanagement.backend.farmers.dto.UpdateFarmerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public FarmerResponse createFarmer(CreateFarmerRequest request) {

        Farmer farmer = new Farmer();

        farmer.setFarmerCode(resolveFarmerCode(request.getFarmerCode()));
        farmer.setName(request.getName());
        farmer.setPhone(request.getPhone());
        farmer.setVillage(request.getVillage());

        Farmer savedFarmer = farmerRepository.save(farmer);

        return new FarmerResponse(savedFarmer);
    }

    public FarmerResponse updateFarmer(
            Long id,
            UpdateFarmerRequest request
    ) {

        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        farmer.setName(request.getName());
        farmer.setPhone(request.getPhone());
        farmer.setVillage(request.getVillage());


        if (request.getStatus() != null) {
            farmer.setStatus(Farmer.FarmerStatus.valueOf(request.getStatus()));
        }

        Farmer updatedFarmer = farmerRepository.save(farmer);

        return new FarmerResponse(updatedFarmer);
    }

    public FarmerResponse getFarmer(Long id) {

        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        return new FarmerResponse(farmer);
    }

    public Page<FarmerResponse> getFarmers(
            String search,
            Pageable pageable
    ) {

        Page<Farmer> farmers;

        if (search == null || search.isBlank()) {

            farmers = farmerRepository.findAll(pageable);

        } else {

            farmers = farmerRepository
                    .findByNameContainingIgnoreCaseOrFarmerCodeContainingIgnoreCaseOrPhoneContainingOrVillageContainingIgnoreCase(
                            search,
                            search,
                            search,
                            search,
                            pageable
                    );
        }

        return farmers.map(FarmerResponse::new);
    }

    /**
     * Uses the client-supplied code when present (rejecting duplicates), otherwise
     * generates a readable sequential code suitable for receipts and searching.
     */
    private String resolveFarmerCode(String requestedCode) {

        if (requestedCode != null && !requestedCode.isBlank()) {
            String code = requestedCode.trim();
            if (farmerRepository.existsByFarmerCode(code)) {
                throw new ConflictException("Farmer code '" + code + "' already exists");
            }
            return code;
        }

        return generateFarmerCode();
    }

    private String generateFarmerCode() {

        long next = farmerRepository.count() + 1;
        String farmerCode;

        do {
            farmerCode = String.format("F-%05d", next++);
        } while (farmerRepository.existsByFarmerCode(farmerCode));

        return farmerCode;
    }
}
