package com.example.demo_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo_test.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
