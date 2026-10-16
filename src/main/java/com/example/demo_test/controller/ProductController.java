package com.example.demo_test.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_test.dto.ApiResponse;
import com.example.demo_test.dto.PageResponse;
import com.example.demo_test.dto.ProductRequest;
import com.example.demo_test.dto.ProductResponse;
import com.example.demo_test.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Products Management")
@RequestMapping("/products")
public class ProductController {

        @Autowired
        private ProductService productService;

        // CREATE
        @Operation(summary = "Create a new product")
        @PostMapping("/create")
        public ApiResponse<ProductResponse> createProduct(
                        @Valid @RequestBody ProductRequest productRequest) {

                ProductResponse createdProduct = productService.createProduct(productRequest);

                return ApiResponse.<ProductResponse>builder()
                                .message("Product successfully created")
                                .data(createdProduct)
                                .build();
        }

        // READ ALL
        @Operation(summary = "Search products with filter, sort and pagination")
        @GetMapping
        public ApiResponse<PageResponse<ProductResponse>> getAllProducts(
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String sort) {

                PageResponse<ProductResponse> products = productService.searchProducts(
                                name, categoryId, minPrice, maxPrice, page, size, sort);

                return ApiResponse.<PageResponse<ProductResponse>>builder()
                                .message("Success fetch all products")
                                .data(products)
                                .build();
        }

        // READ BY ID
        @Operation(summary = "Get product by ID")
        @GetMapping("/{id}")
        public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {

                ProductResponse product = productService.getProductById(id);

                return ApiResponse.<ProductResponse>builder()
                                .message("Success fetch product")
                                .data(product)
                                .build();
        }

        // UPDATE BY ID
        @Operation(summary = "Update product by ID")
        @PutMapping("/update/{id}")
        public ApiResponse<ProductResponse> updateProduct(
                        @PathVariable Long id,
                        @Valid @RequestBody ProductRequest productRequest) {

                ProductResponse updatedProduct = productService.updateProduct(id, productRequest);

                return ApiResponse.<ProductResponse>builder()
                                .message("Product successfully updated")
                                .data(updatedProduct)
                                .build();
        }

        // DELETE BY ID
        @Operation(summary = "Delete product by ID")
        @DeleteMapping("/delete/{id}")
        public ApiResponse<String> deleteProduct(@PathVariable Long id) {

                productService.deleteProduct(id);

                return ApiResponse.<String>builder()
                                .message("Product successfully deleted")
                                .data(null)
                                .build();
        }
}