package com.example.productdataetl.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product entity validation and mapping.
 */
class ProductTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidProduct() {
        // Given
        Product product = new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("999.99"));
        product.setImportDate(LocalDateTime.now());

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertTrue(violations.isEmpty(), "Valid product should have no validation violations");
    }

    @Test
    void testProductWithBlankName() {
        // Given
        Product product = new Product(1L, "", "Description", new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty(), "Product with blank name should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product name cannot be blank")));
    }

    @Test
    void testProductWithNullName() {
        // Given
        Product product = new Product(1L, null, "Description", new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty(), "Product with null name should have validation violations");
    }

    @Test
    void testProductWithNullPrice() {
        // Given
        Product product = new Product(1L, "Test Product", "Description", null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty(), "Product with null price should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product price cannot be null")));
    }

    @Test
    void testProductWithNegativePrice() {
        // Given
        Product product = new Product(1L, "Test Product", "Description", new BigDecimal("-10.00"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty(), "Product with negative price should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product price must be positive")));
    }

    @Test
    void testProductWithZeroPrice() {
        // Given
        Product product = new Product(1L, "Test Product", "Description", BigDecimal.ZERO);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty(), "Product with zero price should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product price must be positive")));
    }

    @Test
    void testProductEqualsAndHashCode() {
        // Given
        Product product1 = new Product(1L, "Laptop", "Description", new BigDecimal("999.99"));
        Product product2 = new Product(1L, "Different Name", "Different Description", new BigDecimal("500.00"));
        Product product3 = new Product(2L, "Laptop", "Description", new BigDecimal("999.99"));

        // Then
        assertEquals(product1, product2, "Products with same ID should be equal");
        assertNotEquals(product1, product3, "Products with different IDs should not be equal");
        assertEquals(product1.hashCode(), product2.hashCode(), "Products with same ID should have same hash code");
    }

    @Test
    void testProductToString() {
        // Given
        Product product = new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("999.99"));
        LocalDateTime importDate = LocalDateTime.of(2023, 12, 1, 10, 30);
        product.setImportDate(importDate);

        // When
        String toString = product.toString();

        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='Laptop'"));
        assertTrue(toString.contains("description='High-performance laptop'"));
        assertTrue(toString.contains("price=999.99"));
        assertTrue(toString.contains("importDate=" + importDate));
    }

    @Test
    void testProductGettersAndSetters() {
        // Given
        Product product = new Product();
        Long id = 1L;
        String name = "Test Product";
        String description = "Test Description";
        BigDecimal price = new BigDecimal("100.00");
        LocalDateTime importDate = LocalDateTime.now();

        // When
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImportDate(importDate);

        // Then
        assertEquals(id, product.getId());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(importDate, product.getImportDate());
    }

    @Test
    void testProductWithNullDescription() {
        // Given
        Product product = new Product(1L, "Test Product", null, new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertTrue(violations.isEmpty(), "Product with null description should be valid (description is optional)");
    }
}