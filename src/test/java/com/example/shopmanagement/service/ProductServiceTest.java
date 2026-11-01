package com.example.shopmanagement.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.shopmanagement.repository.CategoryRepository;
import com.example.shopmanagement.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository);
    }

    @Test
    void searchesByNameAndCategoryWhenBothFiltersAreProvided() {
        when(productRepository.findByNameContainingIgnoreCaseAndCategory_Id("coffee", 3L))
                .thenReturn(List.of());

        productService.search("  coffee  ", 3L);

        verify(productRepository).findByNameContainingIgnoreCaseAndCategory_Id("coffee", 3L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void searchesByNameWhenOnlyNameIsProvided() {
        when(productRepository.findByNameContainingIgnoreCase("tea")).thenReturn(List.of());

        productService.search("tea", null);

        verify(productRepository).findByNameContainingIgnoreCase("tea");
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void filtersByCategoryWhenOnlyCategoryIsProvided() {
        when(productRepository.findByCategory_Id(3L)).thenReturn(List.of());

        productService.search(null, 3L);

        verify(productRepository).findByCategory_Id(3L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void listsAllProductsWhenNoFilterIsProvided() {
        when(productRepository.findAll()).thenReturn(List.of());

        productService.search("  ", null);

        verify(productRepository).findAll();
        verifyNoMoreInteractions(productRepository);
    }
}
