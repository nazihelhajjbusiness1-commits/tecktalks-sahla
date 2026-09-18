package com.farmmanagement.backend.farmers;

import com.farmmanagement.backend.farmers.dto.CreateFarmerRequest;
import com.farmmanagement.backend.farmers.dto.FarmerResponse;
import com.farmmanagement.backend.farmers.dto.UpdateFarmerRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PostMapping
    public FarmerResponse createFarmer(
            @Valid @RequestBody CreateFarmerRequest request
    ) {
        return farmerService.createFarmer(request);
    }

    @GetMapping
    public Page<FarmerResponse> getFarmers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return farmerService.getFarmers(search, pageable);
    }

    @GetMapping("/{id}")
    public FarmerResponse getFarmer(@PathVariable Long id) {
        return farmerService.getFarmer(id);
    }

    @PutMapping("/{id}")
    public FarmerResponse updateFarmer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFarmerRequest request
    ) {
        return farmerService.updateFarmer(id, request);
    }
}