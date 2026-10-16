package com.example.demo_test.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String status;
    private BigDecimal totalPrice;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
