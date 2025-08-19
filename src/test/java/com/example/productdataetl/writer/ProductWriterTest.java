package com.example.productdataetl.writer;

import com.example.productdataetl.model.Product;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductWriter component.
 * Tests JpaItemWriter configuration and basic functionality.
 */
@DataJpaTest
@SpringJUnitConfig
class ProductWriterTest {

    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void testProductWriterConfiguration() {
        // Given
        ProductWriter productWriter = new ProductWriter();

        // When
        JpaItemWriter<Product> writer = productWriter.productJpaItemWriter(entityManagerFactory);

        // Then
        assertNotNull(writer);
        // JpaItemWriter is properly configured and ready to use
    }

    @Test
    void testEntityManagerFactoryInjection() {
        // Test that EntityManagerFactory is properly injected
        assertNotNull(entityManagerFactory);
    }

    @Test
    void testProductEntityPersistence() {
        // Test basic JPA entity persistence using TestEntityManager
        // Given
        Product product = new Product(1L, "Test Product", "Test Description", new BigDecimal("99.99"));
        product.setImportDate(LocalDateTime.now());

        // When
        testEntityManager.persistAndFlush(product);

        // Then
        Product savedProduct = testEntityManager.find(Product.class, 1L);
        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getName());
        assertEquals("Test Description", savedProduct.getDescription());
        assertEquals(new BigDecimal("99.99"), savedProduct.getPrice());
        assertNotNull(savedProduct.getImportDate());
    }

    @Test
    void testProductEntityWithNullDescription() {
        // Test entity persistence with null description
        // Given
        Product product = new Product(2L, "Test Product 2", null, new BigDecimal("49.99"));
        product.setImportDate(LocalDateTime.now());

        // When
        testEntityManager.persistAndFlush(product);

        // Then
        Product savedProduct = testEntityManager.find(Product.class, 2L);
        assertNotNull(savedProduct);
        assertEquals("Test Product 2", savedProduct.getName());
        assertNull(savedProduct.getDescription());
        assertEquals(new BigDecimal("49.99"), savedProduct.getPrice());
    }

    @Test
    void testMultipleProductsPersistence() {
        // Test persisting multiple products
        // Given
        Product product1 = new Product(3L, "Product 1", "Description 1", new BigDecimal("10.00"));
        product1.setImportDate(LocalDateTime.now());
        
        Product product2 = new Product(4L, "Product 2", "Description 2", new BigDecimal("20.00"));
        product2.setImportDate(LocalDateTime.now());

        // When
        testEntityManager.persist(product1);
        testEntityManager.persist(product2);
        testEntityManager.flush();

        // Then
        Product savedProduct1 = testEntityManager.find(Product.class, 3L);
        Product savedProduct2 = testEntityManager.find(Product.class, 4L);
        
        assertNotNull(savedProduct1);
        assertEquals("Product 1", savedProduct1.getName());
        
        assertNotNull(savedProduct2);
        assertEquals("Product 2", savedProduct2.getName());
    }
}