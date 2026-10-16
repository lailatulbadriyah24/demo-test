package com.example.demo_test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_test.dto.ApiResponse;
import com.example.demo_test.dto.OrderResponse;
import com.example.demo_test.dto.PageResponse;
import com.example.demo_test.entity.User;
import com.example.demo_test.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Orders Management")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Checkout a cart into an order")
    @PostMapping("/carts/{cartId}/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @PathVariable Long cartId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponse>builder()
                        .message("Order successfully created")
                        .data(orderService.checkout(cartId, user))
                        .build());
    }

    @Operation(summary = "Get my orders (admin: all orders)")
    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderResponse>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .message("Success fetch orders")
                .data(orderService.getOrders(user, page, size))
                .build();
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {

        return ApiResponse.<OrderResponse>builder()
                .message("Success fetch order")
                .data(orderService.getOrderById(orderId, user))
                .build();
    }

    @Operation(summary = "Pay an order (simulated)")
    @PatchMapping("/orders/{orderId}/pay")
    public ApiResponse<OrderResponse> payOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {

        return ApiResponse.<OrderResponse>builder()
                .message("Order successfully paid")
                .data(orderService.payOrder(orderId, user))
                .build();
    }

    @Operation(summary = "Cancel a pending order and restore stock")
    @PatchMapping("/orders/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {

        return ApiResponse.<OrderResponse>builder()
                .message("Order successfully cancelled")
                .data(orderService.cancelOrder(orderId, user))
                .build();
    }
}
