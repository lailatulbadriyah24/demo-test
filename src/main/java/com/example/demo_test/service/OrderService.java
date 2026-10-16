package com.example.demo_test.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.OrderItemResponse;
import com.example.demo_test.dto.OrderResponse;
import com.example.demo_test.dto.PageResponse;
import com.example.demo_test.entity.Cart;
import com.example.demo_test.entity.CartItem;
import com.example.demo_test.entity.Order;
import com.example.demo_test.entity.OrderItem;
import com.example.demo_test.entity.OrderStatus;
import com.example.demo_test.entity.Products;
import com.example.demo_test.entity.Role;
import com.example.demo_test.entity.User;
import com.example.demo_test.repository.CartItemRepository;
import com.example.demo_test.repository.OrderRepository;
import com.example.demo_test.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse checkout(Long cartId, User user) {

        logger.info("Checkout cart ID {} by user ID {}", cartId, user.getId());

        Cart cart = cartService.getOwnedCart(cartId, user);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        if (cartItems.isEmpty()) {
            logger.warn("Checkout failed: cart {} is empty", cartId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            // Locked read (items are sorted by product ID to avoid deadlocks)
            Products product = productRepository
                    .findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            if (cartItem.getQuantity() > product.getStock()) {
                logger.warn("Checkout failed: insufficient stock for product ID {}", product.getId());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);

            total = total.add(subtotal);
        }

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getId());

        logger.info("Order ID {} created from cart ID {}", saved.getId(), cartId);

        return mapToResponse(saved);
    }

    public PageResponse<OrderResponse> getOrders(User user, int page, int size) {

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        Page<Order> orders = user.getRole() == Role.ADMIN
                ? orderRepository.findAll(pageable)
                : orderRepository.findByUserId(user.getId(), pageable);

        return PageResponse.of(orders.map(this::mapToResponse));
    }

    public OrderResponse getOrderById(Long orderId, User user) {
        return mapToResponse(getAccessibleOrder(orderId, user));
    }

    @Transactional
    public OrderResponse payOrder(Long orderId, User user) {

        Order order = getAccessibleOrder(orderId, user);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PENDING orders can be paid (current status: " + order.getStatus() + ")");
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());

        logger.info("Order ID {} paid", orderId);

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, User user) {

        Order order = getAccessibleOrder(orderId, user);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PENDING orders can be cancelled (current status: " + order.getStatus() + ")");
        }

        order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .forEach(item -> {
                    Products product = productRepository
                            .findByIdForUpdate(item.getProduct().getId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Product not found"));
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                });

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        logger.info("Order ID {} cancelled and stock restored", orderId);

        return mapToResponse(orderRepository.save(order));
    }

    private Order getAccessibleOrder(Long orderId, User user) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order ID {} not found", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
                });

        boolean isOwner = order.getUser().getId().equals(user.getId());

        if (!isOwner && user.getRole() != Role.ADMIN) {
            logger.warn("User ID {} tried to access order ID {}", user.getId(), orderId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this order");
        }

        return order;
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
