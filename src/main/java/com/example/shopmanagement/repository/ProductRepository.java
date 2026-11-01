package com.example.shopmanagement.repository;

import com.example.shopmanagement.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategory_Id(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findByNameContainingIgnoreCaseAndCategory_Id(String name, Long categoryId);

    boolean existsByCategory_Id(Long categoryId);
}
