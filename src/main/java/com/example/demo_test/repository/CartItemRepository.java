package com.example.demo_test.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo_test.entity.CartItem;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId);

    Optional<CartItem> findByIdAndCartId(
            Long id,
            Long cartId);

    void deleteByCartId(Long cartId);
}
