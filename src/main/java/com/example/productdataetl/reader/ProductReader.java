package com.example.productdataetl.reader;

import com.example.productdataetl.model.Product;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration class for creating a JpaPagingItemReader to read Product entities from the database.
 * This reader is used in Step 2 of the ETL pipeline to read products from the database
 * for report generation.
 */
@Configuration
public class ProductReader {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * Creates a JpaPagingItemReader for reading Product entities from the database.
     * 
     * @return JpaPagingItemReader<Product> configured to read all products with proper ordering
     */
    @Bean
    public JpaPagingItemReader<Product> productDatabaseReader() {
        return new JpaPagingItemReaderBuilder<Product>()
                .name("productDatabaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Product p ORDER BY p.id")
                .pageSize(100)
                .build();
    }
}