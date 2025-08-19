package com.example.productdataetl.integration;

import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Step 2: Generate report from database data.
 * Tests the complete step execution with database data and verifies output CSV file.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "batch.output.file=file:test_sales_report.csv"
})
public class Step2IntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Step step2GenerateReportFromDb;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TEST_OUTPUT_FILE = "test_sales_report.csv";

    @BeforeEach
    void setUp() {
        // Remove test output file if it exists
        deleteTestOutputFile();
        
        // Insert test data into database
        insertTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test output file
        deleteTestOutputFile();
    }

    @Test
    void testStep2ExecutionWithDatabaseData() throws Exception {
        // Given: Database contains product data
        
        // When: Execute step2
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step2_generateReportFromDb", jobParameters);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        
        // Then: Verify step execution status
        assertEquals("COMPLETED", stepExecution.getExitStatus().getExitCode());
        assertEquals(0, stepExecution.getSkipCount());
        assertTrue(stepExecution.getReadCount() > 0);
        
        // Verify output CSV file is created
        verifyOutputCsvFile();
    }

    @Test
    void testStep2DataFilteringAndTransformation() throws Exception {
        // Given: Database contains products with various prices
        
        // When: Execute step2
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step2_generateReportFromDb", jobParameters);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        
        // Then: Verify filtering and transformation accuracy
        assertEquals("COMPLETED", stepExecution.getExitStatus().getExitCode());
        
        // Verify that only products with price > 50 are in the output
        verifyFilteringLogic();
        
        // Verify data transformation from Product to SalesReport
        verifyDataTransformation();
    }

    @Test
    void testStep2OutputFileFormat() throws Exception {
        // Given: Database contains product data
        
        // When: Execute step2
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step2_generateReportFromDb", jobParameters);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        
        // Then: Verify output file format
        assertEquals("COMPLETED", stepExecution.getExitStatus().getExitCode());
        
        // Verify CSV file format and headers
        verifyOutputFileFormat();
    }

    private void insertTestData() {
        // Insert products with various prices for filtering test
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Gaming Laptop");
        product1.setDescription("High-performance gaming laptop");
        product1.setPrice(new BigDecimal("1299.99"));
        product1.setImportDate(LocalDateTime.now());
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Wireless Mouse");
        product2.setDescription("Ergonomic wireless mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setImportDate(LocalDateTime.now());
        
        Product product3 = new Product();
        product3.setId(3L);
        product3.setName("Mechanical Keyboard");
        product3.setDescription("RGB backlit mechanical keyboard");
        product3.setPrice(new BigDecimal("89.99"));
        product3.setImportDate(LocalDateTime.now());
        
        Product product4 = new Product();
        product4.setId(4L);
        product4.setName("USB Cable");
        product4.setDescription("Standard USB-C charging cable");
        product4.setPrice(new BigDecimal("12.50"));
        product4.setImportDate(LocalDateTime.now());
        
        Product product5 = new Product();
        product5.setId(5L);
        product5.setName("External Monitor");
        product5.setDescription("27-inch 4K external monitor");
        product5.setPrice(new BigDecimal("399.99"));
        product5.setImportDate(LocalDateTime.now());
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.persist(product4);
        entityManager.persist(product5);
        entityManager.flush();
    }

    private void verifyOutputCsvFile() {
        File outputFile = new File(TEST_OUTPUT_FILE);
        assertTrue(outputFile.exists(), "Output CSV file should be created");
        assertTrue(outputFile.length() > 0, "Output CSV file should not be empty");
    }

    private void verifyFilteringLogic() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        
        // Should have header + filtered products (price > 50)
        assertTrue(lines.size() > 1, "Should have header and at least one filtered product");
        
        // Verify header
        assertEquals("productId,productName,price", lines.get(0));
        
        // Verify that only products with price > 50 are included
        // Expected: Gaming Laptop (1299.99), Mechanical Keyboard (89.99), External Monitor (399.99)
        // Not included: Wireless Mouse (29.99), USB Cable (12.50)
        assertEquals(4, lines.size(), "Should have header + 3 filtered products");
        
        // Verify specific products are included
        String content = String.join("\n", lines);
        assertTrue(content.contains("Gaming Laptop"), "Gaming Laptop should be included (price > 50)");
        assertTrue(content.contains("Mechanical Keyboard"), "Mechanical Keyboard should be included (price > 50)");
        assertTrue(content.contains("External Monitor"), "External Monitor should be included (price > 50)");
        assertFalse(content.contains("Wireless Mouse"), "Wireless Mouse should be filtered out (price <= 50)");
        assertFalse(content.contains("USB Cable"), "USB Cable should be filtered out (price <= 50)");
    }

    private void verifyDataTransformation() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        
        // Verify data transformation from Product to SalesReport format
        for (int i = 1; i < lines.size(); i++) { // Skip header
            String line = lines.get(i);
            String[] fields = line.split(",");
            
            assertEquals(3, fields.length, "Each line should have 3 fields: productId, productName, price");
            
            // Verify field formats
            assertTrue(fields[0].matches("\\d+"), "Product ID should be numeric");
            assertFalse(fields[1].trim().isEmpty(), "Product name should not be empty");
            assertTrue(fields[2].matches("\\d+\\.\\d{2}"), "Price should be in decimal format");
        }
    }

    private void verifyOutputFileFormat() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        
        // Verify header format
        assertEquals("productId,productName,price", lines.get(0));
        
        // Verify CSV format (comma-separated values)
        for (String line : lines) {
            assertTrue(line.contains(","), "Each line should be comma-separated");
        }
        
        // Verify no empty lines
        for (String line : lines) {
            assertFalse(line.trim().isEmpty(), "Should not have empty lines");
        }
    }

    private void deleteTestOutputFile() {
        File outputFile = new File(TEST_OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}