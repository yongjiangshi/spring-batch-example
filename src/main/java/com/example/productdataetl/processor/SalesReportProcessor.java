package com.example.productdataetl.processor;

import com.example.productdataetl.dto.SalesReport;
import com.example.productdataetl.model.Product;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ItemProcessor implementation for filtering and transforming Product entities to SalesReport DTOs.
 * This processor filters products with price greater than 50 and transforms them for report generation.
 */
@Component
public class SalesReportProcessor implements ItemProcessor<Product, SalesReport> {

    private static final BigDecimal PRICE_THRESHOLD = new BigDecimal("50");

    /**
     * Processes a Product entity by filtering based on price and transforming to SalesReport DTO.
     * 
     * @param product the Product entity to process
     * @return SalesReport DTO if product price > 50, null otherwise (filtered out)
     * @throws Exception if processing fails
     */
    @Override
    public SalesReport process(Product product) throws Exception {
        // Filter products with price greater than 50
        if (product.getPrice() == null || product.getPrice().compareTo(PRICE_THRESHOLD) <= 0) {
            // Return null to filter out products with price <= 50
            return null;
        }

        // Transform Product entity to SalesReport DTO
        return new SalesReport(
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}