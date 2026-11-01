package com.example.shopmanagement.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopmanagement.entity.Category;
import com.example.shopmanagement.entity.Product;
import com.example.shopmanagement.service.CategoryService;
import com.example.shopmanagement.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ProductApiController.class, CategoryApiController.class})
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void returnsProductsAsJsonWithCategoryDetails() throws Exception {
        Category category = new Category();
        category.setId(3L);
        category.setName("Đồ uống");

        Product product = new Product();
        product.setId(7L);
        product.setName("Cà phê");
        product.setPrice(25000.0);
        product.setStock(10);
        product.setCategory(category);
        when(productService.search(null, null)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Cà phê"))
                .andExpect(jsonPath("$[0].price").value(25000))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[0].categoryId").value(3))
                .andExpect(jsonPath("$[0].categoryName").value("Đồ uống"));
    }

    @Test
    void returnsCategoriesAsJson() throws Exception {
        Category category = new Category();
        category.setId(3L);
        category.setName("Đồ uống");
        category.setDescription("Danh mục đồ uống");
        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Đồ uống"))
                .andExpect(jsonPath("$[0].description").value("Danh mục đồ uống"));
    }
}
