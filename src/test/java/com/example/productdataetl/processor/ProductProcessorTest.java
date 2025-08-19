package com.example.productdataetl.processor;

import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductProcessor component.
 * Tests data transformation, validation, and cleaning logic.
 */
class ProductProcessorTest {

    private ProductProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ProductProcessor();
    }

    @Test
    void testProcessValidProduct() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        assertNotNull(result.getImportDate());
        assertTrue(result.getImportDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(result.getImportDate().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void testProcessNullProduct() throws Exception {
        // Given
        Product product = null;

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result);
    }

    @Test
    void testProcessProductWithNullId() throws Exception {
        // Given
        Product product = new Product(null, "Test Product", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid ID
    }

    @Test
    void testProcessProductWithZeroId() throws Exception {
        // Given
        Product product = new Product(0L, "Test Product", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid ID
    }

    @Test
    void testProcessProductWithNegativeId() throws Exception {
        // Given
        Product product = new Product(-1L, "Test Product", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid ID
    }

    @Test
    void testProcessProductWithNullName() throws Exception {
        // Given
        Product product = new Product(1L, null, "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid name
    }

    @Test
    void testProcessProductWithEmptyName() throws Exception {
        // Given
        Product product = new Product(1L, "", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid name
    }

    @Test
    void testProcessProductWithWhitespaceOnlyName() throws Exception {
        // Given
        Product product = new Product(1L, "   ", "Test Description", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid name
    }

    @Test
    void testProcessProductWithNullPrice() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", null);

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid price
    }

    @Test
    void testProcessProductWithZeroPrice() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", BigDecimal.ZERO);

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid price
    }

    @Test
    void testProcessProductWithNegativePrice() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", new BigDecimal("-10.00"));

        // When
        Product result = processor.process(product);

        // Then
        assertNull(result); // Should be filtered out due to invalid price
    }

    @Test
    void testDataCleaningTrimsWhitespace() throws Exception {
        // Given
        Product product = new Product(1L, "  Test Product  ", "  Test Description  ", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName()); // Whitespace should be trimmed
        assertEquals("Test Description", result.getDescription()); // Whitespace should be trimmed
    }

    @Test
    void testDataCleaningEmptyDescriptionBecomesNull() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertNull(result.getDescription()); // Empty description should become null
    }

    @Test
    void testDataCleaningWhitespaceOnlyDescriptionBecomesNull() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "   ", new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertNull(result.getDescription()); // Whitespace-only description should become null
    }

    @Test
    void testProcessProductWithNullDescription() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", null, new BigDecimal("99.99"));

        // When
        Product result = processor.process(product);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertNull(result.getDescription()); // Null description should remain null
        assertNotNull(result.getImportDate());
    }

    @Test
    void testImportDateIsSetToCurrentTime() throws Exception {
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", new BigDecimal("99.99"));
        LocalDateTime beforeProcessing = LocalDateTime.now();

        // When
        Product result = processor.process(product);

        // Then
        LocalDateTime afterProcessing = LocalDateTime.now();
        assertNotNull(result);
        assertNotNull(result.getImportDate());
        assertTrue(result.getImportDate().isAfter(beforeProcessing.minusSeconds(1)));
        assertTrue(result.getImportDate().isBefore(afterProcessing.plusSeconds(1)));
    }
}