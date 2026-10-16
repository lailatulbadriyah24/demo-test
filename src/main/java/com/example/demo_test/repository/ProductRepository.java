package com.example.demo_test.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo_test.entity.Products;

import jakarta.persistence.LockModeType;

public interface ProductRepository
        extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {

    boolean existsByCategoryId(Long categoryId);

    // Row lock used by checkout/cancel so concurrent orders cannot oversell stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Products p where p.id = :id")
    Optional<Products> findByIdForUpdate(@Param("id") Long id);
}
