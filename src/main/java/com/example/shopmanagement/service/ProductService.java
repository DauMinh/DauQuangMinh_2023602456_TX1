package com.example.shopmanagement.service;

import com.example.shopmanagement.entity.Category;
import com.example.shopmanagement.entity.Product;
import com.example.shopmanagement.repository.CategoryRepository;
import com.example.shopmanagement.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, Long categoryId) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasName && hasCategory) {
            return productRepository.findByNameContainingIgnoreCaseAndCategory_Id(name.trim(), categoryId);
        }
        if (hasName) {
            return productRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (hasCategory) {
            return productRepository.findByCategory_Id(categoryId);
        }
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có mã " + id));
    }

    @Transactional
    public Product save(Product product, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Vui lòng chọn một danh mục hợp lệ."));
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findById(id));
    }
}
