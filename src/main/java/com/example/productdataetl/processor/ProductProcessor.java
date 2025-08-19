package com.example.productdataetl.processor;

import com.example.productdataetl.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ItemProcessor implementation for transforming Product objects.
 * Adds importDate timestamp and performs data validation and cleaning.
 */
@Component
public class ProductProcessor implements ItemProcessor<Product, Product> {

    private static final Logger logger = LoggerFactory.getLogger(ProductProcessor.class);

    /**
     * Processes a Product item by adding importDate and performing validation.
     * 
     * @param item the Product to process
     * @return the processed Product with importDate set, or null if item should be filtered out
     * @throws Exception if processing fails
     */
    @Override
    public Product process(Product item) throws Exception {
        if (item == null) {
            logger.warn("Received null product item, skipping processing");
            return null;
        }

        // Validate required fields
        if (!isValidProduct(item)) {
            logger.warn("Invalid product data for ID {}: {}", item.getId(), item);
            return null; // Filter out invalid products
        }

        // Set import date to current timestamp
        item.setImportDate(LocalDateTime.now());

        // Perform data cleaning
        cleanProductData(item);

        logger.debug("Processed product: ID={}, Name={}, Price={}", 
                    item.getId(), item.getName(), item.getPrice());

        return item;
    }

    /**
     * Validates that the product has all required fields.
     * 
     * @param product the product to validate
     * @return true if product is valid, false otherwise
     */
    private boolean isValidProduct(Product product) {
        if (product.getId() == null || product.getId() <= 0) {
            logger.warn("Product has invalid ID: {}", product.getId());
            return false;
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            logger.warn("Product has invalid name: {}", product.getName());
            return false;
        }

        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            logger.warn("Product has invalid price: {}", product.getPrice());
            return false;
        }

        return true;
    }

    /**
     * Performs data cleaning operations on the product.
     * 
     * @param product the product to clean
     */
    private void cleanProductData(Product product) {
        // Trim whitespace from name
        if (product.getName() != null) {
            product.setName(product.getName().trim());
        }

        // Trim whitespace from description
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
            // Set empty descriptions to null
            if (product.getDescription().isEmpty()) {
                product.setDescription(null);
            }
        }
    }
}