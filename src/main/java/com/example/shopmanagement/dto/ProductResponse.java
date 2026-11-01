package com.example.shopmanagement.dto;

public record ProductResponse(
        Long id,
        String name,
        Double price,
        Integer stock,
        Long categoryId,
        String categoryName) {
}
