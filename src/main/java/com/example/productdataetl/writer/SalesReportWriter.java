package com.example.productdataetl.writer;

import com.example.productdataetl.dto.SalesReport;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * Configuration class for creating a FlatFileItemWriter to write SalesReport DTOs to CSV file.
 * This writer is used in Step 2 of the ETL pipeline to generate the sales report CSV file.
 */
@Configuration
public class SalesReportWriter {

    /**
     * Creates a FlatFileItemWriter for writing SalesReport DTOs to CSV file.
     * 
     * @return FlatFileItemWriter<SalesReport> configured to write sales report data to CSV
     */
    @Bean
    public FlatFileItemWriter<SalesReport> salesReportCsvWriter() {
        // Configure field extractor to extract fields from SalesReport DTO
        BeanWrapperFieldExtractor<SalesReport> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"productId", "productName", "price"});

        // Configure line aggregator for CSV format
        DelimitedLineAggregator<SalesReport> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<SalesReport>()
                .name("salesReportCsvWriter")
                .resource(new FileSystemResource("sales_report.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("productId,productName,price"))
                .build();
    }
}