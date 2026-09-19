package com.farmmanagement.backend.farmers;

import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.farmers.dto.CreateFarmerRequest;
import com.farmmanagement.backend.farmers.dto.FarmerResponse;
import com.farmmanagement.backend.farmers.dto.UpdateFarmerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public FarmerResponse createFarmer(CreateFarmerRequest request) {

        Farmer farmer = new Farmer();

        farmer.setFarmerCode(generateFarmerCode());
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

    private String generateFarmerCode() {

        String farmerCode;

        do {
            farmerCode = "F-" + UUID.randomUUID();
        } while (farmerRepository.existsByFarmerCode(farmerCode));

        return farmerCode;
    }
}
