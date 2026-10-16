package com.example.demo_test.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.CategoryResponse;
import com.example.demo_test.dto.PageResponse;
import com.example.demo_test.dto.ProductRequest;
import com.example.demo_test.dto.ProductResponse;
import com.example.demo_test.entity.Category;
import com.example.demo_test.entity.Products;
import com.example.demo_test.repository.CategoryRepository;
import com.example.demo_test.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "price", "stock", "createdAt");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // CREATE
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {

        logger.info(
                "Starting to create product with name: {}",
                productRequest.getName());

        Category category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Create failed: Category with ID {} not found",
                            productRequest.getCategoryId());

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found");
                });

        Products product = new Products();

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setCategory(category);

        Products savedProduct = productRepository.save(product);

        logger.info(
                "Product created successfully with ID: {}",
                savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    // SEARCH / FILTER / SORT / PAGINATE
    public PageResponse<ProductResponse> searchProducts(
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        logger.info(
                "Searching products: name={}, categoryId={}, minPrice={}, maxPrice={}, page={}, size={}, sort={}",
                name, categoryId, minPrice, maxPrice, page, size, sort);

        if (page < 0) {
            throw badRequest("Page must be greater than or equal to zero");
        }

        if (size < 1 || size > 100) {
            throw badRequest("Size must be between 1 and 100");
        }

        if (minPrice != null && minPrice.signum() < 0) {
            throw badRequest("minPrice must be greater than or equal to zero");
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw badRequest("maxPrice must be greater than or equal to zero");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw badRequest("minPrice must not be greater than maxPrice");
        }

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        Specification<Products> spec = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Page<ProductResponse> result = productRepository
                .findAll(spec, pageable)
                .map(this::mapToResponse);

        logger.info(
                "Successfully fetched {} products (total {})",
                result.getNumberOfElements(),
                result.getTotalElements());

        return PageResponse.of(result);
    }

    // sort format: "field" or "field,asc|desc"
    private Sort parseSort(String sort) {

        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw badRequest("Sort field must be one of: " + String.join(", ", ALLOWED_SORT_FIELDS));
        }

        Sort.Direction direction = Sort.Direction.ASC;

        if (parts.length > 1) {
            String dir = parts[1].trim();

            if (dir.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            } else if (!dir.equalsIgnoreCase("asc")) {
                throw badRequest("Sort direction must be asc or desc");
            }
        }

        return Sort.by(direction, field).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    // READ BY ID
    public ProductResponse getProductById(Long id) {

        logger.info(
                "Fetching product with ID: {}",
                id);

        Products product = productRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Fetch failed: Product with ID {} not found",
                            id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found");
                });

        logger.info(
                "Product found: {}",
                product.getName());

        return mapToResponse(product);
    }

    // UPDATE BY ID
    @Transactional
    public ProductResponse updateProduct(
            Long id,
            ProductRequest productRequest) {

        logger.info(
                "Starting update process for product ID: {}",
                id);

        Products product = productRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Update failed: Product with ID {} not found",
                            id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found");
                });

        Category category = categoryRepository.findById(
                productRequest.getCategoryId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Update failed: Category with ID {} not found",
                            productRequest.getCategoryId());

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found");
                });

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setCategory(category);

        Products updatedProduct = productRepository.save(product);

        logger.info(
                "Product with ID: {} updated successfully",
                updatedProduct.getId());

        return mapToResponse(updatedProduct);
    }

    // DELETE BY ID
    @Transactional
    public void deleteProduct(Long id) {

        logger.info(
                "Attempting to delete product with ID: {}",
                id);

        if (!productRepository.existsById(id)) {

            logger.warn(
                    "Delete failed: Product with ID {} does not exist",
                    id);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found");
        }

        productRepository.deleteById(id);

        logger.info(
                "Product with ID: {} deleted successfully",
                id);
    }

    // MAPPING ENTITY TO RESPONSE
    private ProductResponse mapToResponse(Products product) {

        Category category = product.getCategory();

        CategoryResponse categoryResponse = null;

        if (category != null) {

            categoryResponse = CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(categoryResponse)
                .build();
    }
}