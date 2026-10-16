package com.example.demo_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.example.demo_test.dto.CartItemRequest;
import com.example.demo_test.dto.CartResponse;
import com.example.demo_test.entity.User;
import com.example.demo_test.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Carts Management")
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "Create a new cart for the logged-in user")
    @PostMapping("/create")
    public ApiResponse<CartResponse> createCart(
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.createCart(user);

        return ApiResponse.<CartResponse>builder()
                .message("Cart successfully created")
                .data(cart)
                .build();
    }

    @Operation(summary = "Get cart by ID")
    @GetMapping("/{cartId}")
    public ApiResponse<CartResponse> getCartById(
            @PathVariable Long cartId,
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.getCartById(cartId, user);

        return ApiResponse.<CartResponse>builder()
                .message("Success fetch cart")
                .data(cart)
                .build();
    }

    @Operation(summary = "Add product to cart")
    @PostMapping("/{cartId}/items")
    public ApiResponse<CartResponse> addProductToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.addProductToCart(
                cartId,
                request,
                user);

        return ApiResponse.<CartResponse>builder()
                .message("Product successfully added to cart")
                .data(cart)
                .build();
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/{cartId}/items/{itemId}")
    public ApiResponse<CartResponse> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.updateCartItem(
                cartId,
                itemId,
                quantity,
                user);

        return ApiResponse.<CartResponse>builder()
                .message(
                        "Cart item quantity successfully updated")
                .data(cart)
                .build();
    }

    @Operation(summary = "Remove product from cart")
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ApiResponse<CartResponse> removeCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.removeCartItem(
                cartId,
                itemId,
                user);

        return ApiResponse.<CartResponse>builder()
                .message(
                        "Product successfully removed from cart")
                .data(cart)
                .build();
    }

    @Operation(summary = "Remove all items from cart")
    @DeleteMapping("/{cartId}/items")
    public ApiResponse<CartResponse> clearCart(
            @PathVariable Long cartId,
            @AuthenticationPrincipal User user) {

        CartResponse cart = cartService.clearCart(cartId, user);

        return ApiResponse.<CartResponse>builder()
                .message("Cart successfully cleared")
                .data(cart)
                .build();
    }
}
