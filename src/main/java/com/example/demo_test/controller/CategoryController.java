package com.example.demo_test.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_test.dto.ApiResponse;
import com.example.demo_test.dto.CategoryRequest;
import com.example.demo_test.dto.CategoryResponse;
import com.example.demo_test.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Categories Management")
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // CREATE
    @Operation(summary = "Create a new category")
    @PostMapping("/create")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest) {

        CategoryResponse createdCategory = categoryService.createCategory(categoryRequest);

        return ApiResponse.<CategoryResponse>builder()
                .message("Category successfully created")
                .data(createdCategory)
                .build();
    }

    // READ ALL
    @Operation(summary = "Get all categories")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categories = categoryService.getAllCategories();

        return ApiResponse.<List<CategoryResponse>>builder()
                .message("Success fetch all categories")
                .data(categories)
                .build();
    }

    // READ BY ID
    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(
            @PathVariable Long id) {

        CategoryResponse category = categoryService.getCategoryById(id);

        return ApiResponse.<CategoryResponse>builder()
                .message("Success fetch category")
                .data(category)
                .build();
    }

    // UPDATE BY ID
    @Operation(summary = "Update category by ID")
    @PutMapping("/update/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        CategoryResponse updatedCategory = categoryService.updateCategory(
                id,
                categoryRequest);

        return ApiResponse.<CategoryResponse>builder()
                .message("Category successfully updated")
                .data(updatedCategory)
                .build();
    }

    // DELETE BY ID
    @Operation(summary = "Delete category by ID")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ApiResponse.<String>builder()
                .message("Category successfully deleted")
                .data(null)
                .build();
    }
}