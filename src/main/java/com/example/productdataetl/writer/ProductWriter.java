package com.example.productdataetl.writer;

import com.example.productdataetl.model.Product;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for creating ProductWriter component.
 * Implements JpaItemWriter<Product> for persisting Product entities to database.
 */
@Configuration
public class ProductWriter {

    /**
     * Creates a JpaItemWriter bean for persisting Product entities to database.
     * Configures EntityManagerFactory injection for database operations.
     * 
     * @param entityManagerFactory the EntityManagerFactory for JPA operations
     * @return JpaItemWriter<Product> configured for database persistence
     */
    @Bean
    public JpaItemWriter<Product> productJpaItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Product>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}