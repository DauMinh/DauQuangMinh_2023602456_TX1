package com.example.shopmanagement.controller;

import com.example.shopmanagement.entity.Category;
import com.example.shopmanagement.entity.Product;
import com.example.shopmanagement.service.CategoryService;
import com.example.shopmanagement.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute("products", productService.search(name, categoryId));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("activeNav", "products");
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        return showForm(model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setCategoryId(product.getCategory().getId());
        model.addAttribute("productForm", form);
        model.addAttribute("productId", id);
        return showForm(model);
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @Valid @ModelAttribute("productForm") ProductForm form,
                       BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (id != null) {
                model.addAttribute("productId", id);
            }
            model.addAttribute("activeNav", "products");
            return showForm(model);
        }

        Product product = id == null ? new Product() : productService.findById(id);
        product.setName(form.getName());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        productService.save(product, form.getCategoryId());
        redirectAttributes.addFlashAttribute("successMessage",
                id == null ? "Đã thêm sản phẩm thành công." : "Đã cập nhật sản phẩm thành công.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm.");
        return "redirect:/products";
    }

    private String showForm(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("activeNav", "products");
        if (!model.containsAttribute("productId")) {
            model.addAttribute("productId", null);
        }
        model.addAttribute("pageTitle", model.getAttribute("productId") == null
                ? "Thêm sản phẩm" : "Sửa sản phẩm");
        return "products/form";
    }
}
