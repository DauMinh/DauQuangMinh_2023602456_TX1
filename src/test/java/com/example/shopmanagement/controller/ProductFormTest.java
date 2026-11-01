package com.example.shopmanagement.controller;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductFormTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void rejectsBlankNameNonPositivePriceAndMissingCategory() {
        ProductForm form = new ProductForm();
        form.setName(" ");
        form.setPrice(0.0);

        var violations = validator.validate(form);

        assertThat(violations).hasSize(3);
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "price", "categoryId");
    }

    @Test
    void acceptsValidProductFields() {
        ProductForm form = new ProductForm();
        form.setName("Cà phê");
        form.setPrice(25000.0);
        form.setStock(10);
        form.setCategoryId(1L);

        assertThat(validator.validate(form)).isEmpty();
    }
}
