package com.example.demo_test.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.CategoryRequest;
import com.example.demo_test.dto.CategoryResponse;
import com.example.demo_test.entity.Category;
import com.example.demo_test.repository.CategoryRepository;
import com.example.demo_test.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // CREATE
    @Transactional
    public CategoryResponse createCategory(
            CategoryRequest categoryRequest) {

        logger.info(
                "Starting to create category with name: {}",
                categoryRequest.getName());

        Category category = new Category();

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category savedCategory = categoryRepository.save(category);

        logger.info(
                "Category created successfully with ID: {}",
                savedCategory.getId());

        return mapToResponse(savedCategory);
    }

    // READ ALL
    public List<CategoryResponse> getAllCategories() {

        logger.info(
                "Fetching all categories from database");

        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        logger.info(
                "Successfully fetched {} categories",
                categories.size());

        return categories;
    }

    // READ BY ID
    public CategoryResponse getCategoryById(Long id) {

        logger.info(
                "Fetching category with ID: {}",
                id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Fetch failed: Category with ID {} not found",
                            id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found");
                });

        logger.info(
                "Category found: {}",
                category.getName());

        return mapToResponse(category);
    }

    // UPDATE BY ID
    @Transactional
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest categoryRequest) {

        logger.info(
                "Starting update process for category ID: {}",
                id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Update failed: Category with ID {} not found",
                            id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found");
                });

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        logger.info(
                "Category with ID: {} updated successfully",
                updatedCategory.getId());

        return mapToResponse(updatedCategory);
    }

    // DELETE BY ID
    @Transactional
    public void deleteCategory(Long id) {

        logger.info(
                "Attempting to delete category with ID: {}",
                id);

        if (!categoryRepository.existsById(id)) {

            logger.warn(
                    "Delete failed: Category with ID {} does not exist",
                    id);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category not found");
        }

        if (productRepository.existsByCategoryId(id)) {

            logger.warn(
                    "Delete failed: Category with ID {} still has products",
                    id);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category still has products and cannot be deleted");
        }

        categoryRepository.deleteById(id);

        logger.info(
                "Category with ID: {} deleted successfully",
                id);
    }

    // MAPPING ENTITY TO RESPONSE
    private CategoryResponse mapToResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}