package com.example.productdataetl.reader;

import com.example.productdataetl.model.Product;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Configuration class for creating ProductCsvReader component.
 * Implements FlatFileItemReader<Product> for reading CSV files and mapping to Product objects.
 */
@Configuration
public class ProductCsvReader {

    /**
     * Creates a FlatFileItemReader bean for reading products from CSV file.
     * Configures DelimitedLineTokenizer and BeanWrapperFieldSetMapper for CSV parsing.
     * 
     * @return FlatFileItemReader<Product> configured for products.csv
     */
    @Bean
    public FlatFileItemReader<Product> productCsvItemReader() {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productCsvItemReader")
                .resource(new ClassPathResource("products.csv"))
                .linesToSkip(1) // Skip header line
                .delimited()
                .delimiter(",")
                .names("id", "name", "description", "price")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {{
                    setTargetType(Product.class);
                }})
                .build();
    }
}