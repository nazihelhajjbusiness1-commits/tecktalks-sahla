package com.farmmanagement.backend.products;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.products.dto.CreateProductRequest;
import com.farmmanagement.backend.products.dto.ProductResponse;
import com.farmmanagement.backend.products.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsByNameIgnoreCaseAndVarietyIgnoreCase(
                request.getName(),
                request.getVariety()
        )) {
            throw new ConflictException(
                    "A product with this name and variety already exists"
            );
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setVariety(request.getVariety());
        product.setUnit(request.getUnit());

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (productRepository.existsByNameIgnoreCaseAndVarietyIgnoreCaseAndIdNot(
                request.getName(),
                request.getVariety(),
                id
        )) {
            throw new ConflictException(
                    "A product with this name and variety already exists"
            );
        }

        product.setName(request.getName());
        product.setVariety(request.getVariety());
        product.setUnit(request.getUnit());
        product.setActive(request.isActive());

        Product updatedProduct = productRepository.save(product);

        return new ProductResponse(updatedProduct);
    }

    public ProductResponse getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return new ProductResponse(product);
    }

    public Page<ProductResponse> getProducts(
            String search,
            Pageable pageable
    ) {

        Page<Product> products;

        if (search == null || search.isBlank()) {

            products = productRepository.findAll(pageable);

        } else {

            products = productRepository
                    .findByNameContainingIgnoreCaseOrVarietyContainingIgnoreCase(
                            search,
                            search,
                            pageable
                    );
        }

        return products.map(ProductResponse::new);
    }
}
