package com.example.productdataetl.reader;

import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductCsvReader component.
 * Tests CSV reading and Product object mapping functionality.
 */
class ProductCsvReaderTest {

    private FlatFileItemReader<Product> reader;
    private ExecutionContext executionContext;

    @BeforeEach
    void setUp() {
        // Create reader with same configuration as ProductCsvReader
        reader = new FlatFileItemReaderBuilder<Product>()
                .name("testProductCsvItemReader")
                .resource(new ClassPathResource("products.csv"))
                .linesToSkip(1) // Skip header line
                .delimited()
                .delimiter(",")
                .names("id", "name", "description", "price")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {{
                    setTargetType(Product.class);
                }})
                .build();

        executionContext = new ExecutionContext();
    }

    @Test
    void testReadFirstProduct() throws Exception {
        // Given
        reader.open(executionContext);

        // When
        Product product = reader.read();

        // Then
        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Gaming Laptop", product.getName());
        assertEquals("High-performance gaming laptop with RTX graphics", product.getDescription());
        assertEquals(new BigDecimal("1299.99"), product.getPrice());
        assertNull(product.getImportDate()); // Should be null initially

        reader.close();
    }

    @Test
    void testReadMultipleProducts() throws Exception {
        // Given
        reader.open(executionContext);

        // When - Read first few products
        Product product1 = reader.read();
        Product product2 = reader.read();
        Product product3 = reader.read();

        // Then
        assertNotNull(product1);
        assertEquals(1L, product1.getId());
        assertEquals("Gaming Laptop", product1.getName());

        assertNotNull(product2);
        assertEquals(2L, product2.getId());
        assertEquals("Wireless Mouse", product2.getName());
        assertEquals(new BigDecimal("29.99"), product2.getPrice());

        assertNotNull(product3);
        assertEquals(3L, product3.getId());
        assertEquals("Mechanical Keyboard", product3.getName());

        reader.close();
    }

    @Test
    void testReadAllProducts() throws Exception {
        // Given
        reader.open(executionContext);
        int productCount = 0;

        // When - Read all products
        Product product;
        while ((product = reader.read()) != null) {
            productCount++;
            assertNotNull(product.getId());
            assertNotNull(product.getName());
            assertNotNull(product.getPrice());
            assertTrue(product.getPrice().compareTo(BigDecimal.ZERO) > 0);
        }

        // Then - Should read 15 products from the CSV file
        assertEquals(15, productCount);

        reader.close();
    }

    @Test
    void testProductFieldMapping() throws Exception {
        // Given
        reader.open(executionContext);

        // When - Read a specific product (skip to product with known values)
        Product product = null;
        for (int i = 0; i < 4; i++) { // Read to get the USB Cable product
            product = reader.read();
        }

        // Then - Verify field mapping for USB Cable (id=4)
        assertNotNull(product);
        assertEquals(4L, product.getId());
        assertEquals("USB Cable", product.getName());
        assertEquals("Standard USB-C charging cable", product.getDescription());
        assertEquals(new BigDecimal("12.50"), product.getPrice());

        reader.close();
    }

    @Test
    void testReaderConfiguration() {
        // Test that reader is properly configured
        assertNotNull(reader);
        assertEquals("testProductCsvItemReader", reader.getName());
    }
}