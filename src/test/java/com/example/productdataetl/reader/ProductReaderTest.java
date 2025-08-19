package com.example.productdataetl.reader;

import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductReader configuration class.
 * Tests the JpaPagingItemReader configuration for database access.
 */
@ExtendWith(MockitoExtension.class)
class ProductReaderTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private ProductReader productReader;

    @BeforeEach
    void setUp() {
        productReader = new ProductReader();
        ReflectionTestUtils.setField(productReader, "entityManagerFactory", entityManagerFactory);
    }

    @Test
    void testProductDatabaseReaderConfiguration() {
        // When
        JpaPagingItemReader<Product> reader = productReader.productDatabaseReader();

        // Then
        assertNotNull(reader, "Reader should not be null");
        assertEquals("productDatabaseReader", reader.getName());
        
        // Verify the query string is set correctly
        String expectedQuery = "SELECT p FROM Product p ORDER BY p.id";
        assertEquals(expectedQuery, ReflectionTestUtils.getField(reader, "queryString"));
        
        // Verify page size is set correctly
        assertEquals(100, ReflectionTestUtils.getField(reader, "pageSize"));
        
        // Verify entity manager factory is set
        assertEquals(entityManagerFactory, ReflectionTestUtils.getField(reader, "entityManagerFactory"));
    }

    @Test
    void testReaderName() {
        // When
        JpaPagingItemReader<Product> reader = productReader.productDatabaseReader();

        // Then
        assertEquals("productDatabaseReader", reader.getName());
    }

    @Test
    void testPageSize() {
        // When
        JpaPagingItemReader<Product> reader = productReader.productDatabaseReader();

        // Then
        Integer pageSize = (Integer) ReflectionTestUtils.getField(reader, "pageSize");
        assertEquals(100, pageSize, "Page size should be set to 100 for efficient memory usage");
    }

    @Test
    void testQueryString() {
        // When
        JpaPagingItemReader<Product> reader = productReader.productDatabaseReader();

        // Then
        String queryString = (String) ReflectionTestUtils.getField(reader, "queryString");
        assertEquals("SELECT p FROM Product p ORDER BY p.id", queryString, 
                "Query should select all products ordered by ID");
    }

    @Test
    void testEntityManagerFactoryInjection() {
        // When
        JpaPagingItemReader<Product> reader = productReader.productDatabaseReader();

        // Then
        EntityManagerFactory injectedFactory = (EntityManagerFactory) ReflectionTestUtils.getField(reader, "entityManagerFactory");
        assertEquals(entityManagerFactory, injectedFactory, 
                "EntityManagerFactory should be properly injected");
    }
}