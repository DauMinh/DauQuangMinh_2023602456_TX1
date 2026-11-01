package com.example.shopmanagement.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

@WebMvcTest({HomeController.class, CategoryController.class, ProductController.class})
class PageRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ProductService productService;

    @Test
    void rendersProductListWithCategoryName() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Đồ uống");

        Product product = new Product();
        product.setId(1L);
        product.setName("Cà phê");
        product.setPrice(25000.0);
        product.setStock(5);
        product.setCategory(category);

        given(productService.search(null, null)).willReturn(List.of(product));
        given(categoryService.findAll()).willReturn(List.of(category));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cà phê")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("25.000 ₫")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đồ uống")));
    }

    @Test
    void showsValidationErrorsOnProductForm() throws Exception {
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(post("/products/save")
                        .param("name", " ")
                        .param("price", "0")
                        .param("categoryId", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tên sản phẩm không được để trống")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Giá phải lớn hơn 0")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Vui lòng chọn danh mục")));
    }

    @Test
    void rendersCategoryList() throws Exception {
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thêm danh mục")));
    }

    @Test
    void rendersProductEmptyStateAndKeepsSearchAndCategoryFilters() throws Exception {
        given(productService.search("khong co", 4L)).willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/products").param("name", "khong co").param("categoryId", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chưa tìm thấy sản phẩm")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("khong co")));

        verify(productService).search("khong co", 4L);
    }

    @Test
    void rendersProductCreateFormWithCategoryOptions() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Đồ uống");
        given(categoryService.findAll()).willReturn(List.of(category));

        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thông tin sản phẩm")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đồ uống")));
    }

    @Test
    void rendersProductEditFormWithExistingValues() throws Exception {
        Category category = new Category();
        category.setId(3L);
        category.setName("Đồ uống");
        Product product = new Product();
        product.setId(12L);
        product.setName("Coffee");
        product.setPrice(25000.0);
        product.setStock(8);
        product.setCategory(category);
        given(productService.findById(12L)).willReturn(product);
        given(categoryService.findAll()).willReturn(List.of(category));

        mockMvc.perform(get("/products/12/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Coffee\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"25000")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"8\"")));
    }

    @Test
    void rendersCategoryCreateForm() throws Exception {
        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thông tin danh mục")));
    }

    @Test
    void rendersCategoryEditFormWithExistingValues() throws Exception {
        Category category = new Category();
        category.setId(4L);
        category.setName("Đồ uống");
        category.setDescription("Sản phẩm pha chế");
        given(categoryService.findById(4L)).willReturn(category);

        mockMvc.perform(get("/categories/4/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Đồ uống\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sản phẩm pha chế")));
    }

    @Test
    void showsCategoryValidationErrorInForm() throws Exception {
        mockMvc.perform(post("/categories/save").param("name", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tên danh mục không được để trống")));
    }

    @Test
    void createsCategoryAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/categories/save")
                        .param("name", "Đồ uống")
                        .param("description", "Thử nghiệm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/categories"));

        verify(categoryService).save(argThat(category ->
                "Đồ uống".equals(category.getName()) && "Thử nghiệm".equals(category.getDescription())));
    }

    @Test
    void createsProductAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/products/save")
                        .param("name", "Cà phê")
                        .param("price", "25000")
                        .param("stock", "10")
                        .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/products"));

        verify(productService).save(argThat(product ->
                "Cà phê".equals(product.getName())
                        && Double.valueOf(25000).equals(product.getPrice())
                        && Integer.valueOf(10).equals(product.getStock())), eq(1L));
    }

    @Test
    void deletesProductAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/products/12/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/products"));

        verify(productService).delete(12L);
    }
}
