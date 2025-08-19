package com.example.productdataetl.config;

import com.example.productdataetl.dto.SalesReport;
import com.example.productdataetl.exception.InvalidCsvRecordException;
import com.example.productdataetl.exception.TransientDatabaseException;
import com.example.productdataetl.listener.DetailedJobExecutionListener;
import com.example.productdataetl.listener.DetailedStepExecutionListener;
import com.example.productdataetl.model.Product;
import com.example.productdataetl.processor.ProductProcessor;
import com.example.productdataetl.processor.SalesReportProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration class for the Product ETL pipeline.
 * Defines job and step configurations with proper chunk-oriented processing,
 * transaction management, and error handling.
 */
@Configuration
public class BatchConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);
    
    @Value("${batch.chunk.size:10}")
    private int chunkSize;
    
    @Value("${batch.skip.limit:5}")
    private int skipLimit;
    
    @Value("${batch.retry.limit:3}")
    private int retryLimit;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Step 1 components
    @Autowired
    private FlatFileItemReader<Product> productCsvItemReader;

    @Autowired
    private ProductProcessor productProcessor;

    @Autowired
    private JpaItemWriter<Product> productJpaItemWriter;

    // Step 2 components
    @Autowired
    private JpaPagingItemReader<Product> productDatabaseReader;

    @Autowired
    private SalesReportProcessor salesReportProcessor;

    @Autowired
    private FlatFileItemWriter<SalesReport> salesReportCsvWriter;
   
    /**
     * Creates a custom skip policy bean for handling different types of exceptions.
     * 
     * @return CustomSkipPolicy configured with skip limit
     */
    @Bean
    public CustomSkipPolicy customSkipPolicy() {
        return new CustomSkipPolicy(skipLimit);
    }
    
    /**
     * Creates a custom retry policy bean for handling transient errors.
     * 
     * @return CustomRetryPolicy configured with retry limit
     */
    @Bean
    public CustomRetryPolicy customRetryPolicy() {
        return new CustomRetryPolicy(retryLimit);
    }

    /**
     * Defines Step 1: Load CSV data to database.
     * Configures chunk-oriented processing with reader, processor, and writer.
     * Includes enhanced skip policy, retry logic, and detailed step execution listeners.
     * 
     * @return Step bean for step1_loadCsvToDb
     */
    @Bean
    public Step step1LoadCsvToDb() {
        return new StepBuilder("step1_loadCsvToDb", jobRepository)
                .<Product, Product>chunk(chunkSize, transactionManager)
                .reader(productCsvItemReader)
                .processor(productProcessor)
                .writer(productJpaItemWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy())
                .skip(FlatFileParseException.class)
                .skip(InvalidCsvRecordException.class)
                .skip(DataAccessException.class)
                .noSkip(TransientDatabaseException.class)
                .retryPolicy(customRetryPolicy())
                .retry(TransientDataAccessException.class)
                .retry(TransientDatabaseException.class)
                .noRetry(FlatFileParseException.class)
                .noRetry(InvalidCsvRecordException.class)
                .listener(new DetailedStepExecutionListener())
                .build();
    }
  
    /**
     * Defines Step 2: Generate report from database data.
     * Configures chunk-oriented processing with reader, processor, and writer.
     * Includes enhanced error handling policies and detailed step execution listeners for progress tracking.
     * 
     * @return Step bean for step2_generateReportFromDb
     */
    @Bean
    public Step step2GenerateReportFromDb() {
        return new StepBuilder("step2_generateReportFromDb", jobRepository)
                .<Product, SalesReport>chunk(chunkSize, transactionManager)
                .reader(productDatabaseReader)
                .processor(salesReportProcessor)
                .writer(salesReportCsvWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy())
                .skip(DataAccessException.class)
                .noSkip(TransientDatabaseException.class)
                .retryPolicy(customRetryPolicy())
                .retry(TransientDataAccessException.class)
                .retry(TransientDatabaseException.class)
                .listener(new DetailedStepExecutionListener())
                .build();
    }

    /**
     * Defines the main ETL Job that executes step1 followed by step2.
     * Configures job flow with proper step sequencing and enhanced job execution listeners
     * for comprehensive job monitoring and error reporting.
     * 
     * @return Job bean for productEtlJob
     */
    @Bean
    public Job productEtlJob() {
        return new JobBuilder("productEtlJob", jobRepository)
                .start(step1LoadCsvToDb())
                .next(step2GenerateReportFromDb())
                .listener(new DetailedJobExecutionListener())
                .build();
    }
}