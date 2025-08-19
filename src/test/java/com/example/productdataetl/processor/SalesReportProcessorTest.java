package com.example.productdataetl.processor;

import com.example.productdataetl.dto.SalesReport;
import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SalesReportProcessor.
 * Tests the filtering logic and DTO transformation functionality.
 */
class SalesReportProcessorTest {

    private SalesReportProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SalesReportProcessor();
    }

    @Test
    void testProcessProductWithPriceGreaterThan50() throws Exception {
        // Given
        Product product = new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("999.99"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNotNull(result, "Result should not be null for products with price > 50");
        assertEquals(1L, result.getProductId());
        assertEquals("Laptop", result.getProductName());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
    }

    @Test
    void testProcessProductWithPriceEqualTo50() throws Exception {
        // Given
        Product product = new Product(2L, "Keyboard", "Mechanical keyboard", new BigDecimal("50.00"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNull(result, "Result should be null for products with price = 50 (filtered out)");
    }

    @Test
    void testProcessProductWithPriceLessThan50() throws Exception {
        // Given
        Product product = new Product(3L, "Mouse", "Wireless mouse", new BigDecimal("25.50"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNull(result, "Result should be null for products with price < 50 (filtered out)");
    }

    @Test
    void testProcessProductWithNullPrice() throws Exception {
        // Given
        Product product = new Product(4L, "Unknown", "Product with no price", null);
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNull(result, "Result should be null for products with null price (filtered out)");
    }

    @Test
    void testProcessProductWithPriceJustAbove50() throws Exception {
        // Given
        Product product = new Product(5L, "Headphones", "Wireless headphones", new BigDecimal("50.01"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNotNull(result, "Result should not be null for products with price just above 50");
        assertEquals(5L, result.getProductId());
        assertEquals("Headphones", result.getProductName());
        assertEquals(new BigDecimal("50.01"), result.getPrice());
    }

    @Test
    void testProcessProductWithHighPrice() throws Exception {
        // Given
        Product product = new Product(6L, "Server", "Enterprise server", new BigDecimal("5000.00"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNotNull(result, "Result should not be null for products with high price");
        assertEquals(6L, result.getProductId());
        assertEquals("Server", result.getProductName());
        assertEquals(new BigDecimal("5000.00"), result.getPrice());
    }

    @Test
    void testDtoTransformation() throws Exception {
        // Given
        Product product = new Product(7L, "Tablet", "10-inch tablet", new BigDecimal("299.99"));
        product.setImportDate(LocalDateTime.now());

        // When
        SalesReport result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getProductId(), "Product ID should be correctly mapped");
        assertEquals(product.getName(), result.getProductName(), "Product name should be correctly mapped");
        assertEquals(product.getPrice(), result.getPrice(), "Product price should be correctly mapped");
    }

    @Test
    void testFilteringBoundaryConditions() throws Exception {
        // Test exactly at boundary
        Product productAt50 = new Product(8L, "Item1", "Description", new BigDecimal("50.00"));
        assertNull(processor.process(productAt50), "Product with price exactly 50 should be filtered out");

        // Test just below boundary
        Product productBelow50 = new Product(9L, "Item2", "Description", new BigDecimal("49.99"));
        assertNull(processor.process(productBelow50), "Product with price 49.99 should be filtered out");

        // Test just above boundary
        Product productAbove50 = new Product(10L, "Item3", "Description", new BigDecimal("50.01"));
        assertNotNull(processor.process(productAbove50), "Product with price 50.01 should not be filtered out");
    }
}