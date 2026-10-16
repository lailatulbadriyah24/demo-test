package com.example.demo_test.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.CartItemRequest;
import com.example.demo_test.dto.CartItemResponse;
import com.example.demo_test.dto.CartResponse;
import com.example.demo_test.entity.Cart;
import com.example.demo_test.entity.CartItem;
import com.example.demo_test.entity.Products;
import com.example.demo_test.entity.User;
import com.example.demo_test.repository.CartItemRepository;
import com.example.demo_test.repository.CartRepository;
import com.example.demo_test.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public CartResponse createCart(User user) {

        logger.info("Starting to create cart for user ID: {}", user.getId());

        Cart cart = new Cart();
        cart.setUser(user);

        Cart savedCart = cartRepository.save(cart);

        logger.info(
                "Cart created successfully with ID: {}",
                savedCart.getId());

        return mapToResponse(savedCart);
    }

    public CartResponse getCartById(Long cartId, User user) {

        logger.info("Fetching cart with ID: {}", cartId);

        return mapToResponse(getOwnedCart(cartId, user));
    }

    @Transactional
    public CartResponse addProductToCart(
            Long cartId,
            CartItemRequest request,
            User user) {

        logger.info(
                "Adding product ID {} to cart ID {}",
                request.getProductId(),
                cartId);

        Cart cart = getOwnedCart(cartId, user);

        Products product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> {
                    logger.warn(
                            "Product with ID {} not found",
                            request.getProductId());

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found");
                });

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cartId,
                        request.getProductId())
                .orElse(null);

        if (cartItem != null) {

            int newQuantity = cartItem.getQuantity()
                    + request.getQuantity();

            validateStock(product, newQuantity);

            cartItem.setQuantity(newQuantity);

            logger.info(
                    "Product already exists. Quantity updated to {}",
                    newQuantity);

        } else {

            validateStock(
                    product,
                    request.getQuantity());

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());

            logger.info(
                    "New product added to cart");
        }

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(
            Long cartId,
            Long itemId,
            Integer quantity,
            User user) {

        logger.info(
                "Updating cart item ID {} in cart ID {}",
                itemId,
                cartId);

        Cart cart = getOwnedCart(cartId, user);

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        CartItem cartItem = findCartItem(itemId, cartId);

        validateStock(
                cartItem.getProduct(),
                quantity);

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(
            Long cartId,
            Long itemId,
            User user) {

        logger.info(
                "Removing cart item ID {} from cart ID {}",
                itemId,
                cartId);

        Cart cart = getOwnedCart(cartId, user);

        CartItem cartItem = findCartItem(itemId, cartId);

        cartItemRepository.delete(cartItem);

        logger.info(
                "Cart item ID {} removed successfully",
                itemId);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(Long cartId, User user) {

        logger.info("Clearing all items from cart ID {}", cartId);

        Cart cart = getOwnedCart(cartId, user);

        cartItemRepository.deleteByCartId(cartId);

        return mapToResponse(cart);
    }

    /** Finds a cart and makes sure it belongs to the given user (404 if missing, 403 if not theirs). */
    public Cart getOwnedCart(Long cartId, User user) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    logger.warn(
                            "Cart with ID {} not found",
                            cartId);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found");
                });

        if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
            logger.warn(
                    "User ID {} tried to access cart ID {} that is not theirs",
                    user.getId(),
                    cartId);

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this cart");
        }

        return cart;
    }

    private CartItem findCartItem(Long itemId, Long cartId) {

        return cartItemRepository
                .findByIdAndCartId(itemId, cartId)
                .orElseThrow(() -> {
                    logger.warn(
                            "Cart item ID {} not found in cart {}",
                            itemId,
                            cartId);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart item not found");
                });
    }

    private void validateStock(
            Products product,
            Integer quantity) {

        if (quantity > product.getStock()) {

            logger.warn(
                    "Insufficient stock for product ID {}",
                    product.getId());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient product stock");
        }
    }

    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> items = cartItemRepository
                .findByCartId(cart.getId())
                .stream()
                .map(this::mapItemToResponse)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }

    private CartItemResponse mapItemToResponse(
            CartItem cartItem) {

        Products product = cartItem.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(
                        BigDecimal.valueOf(
                                cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
