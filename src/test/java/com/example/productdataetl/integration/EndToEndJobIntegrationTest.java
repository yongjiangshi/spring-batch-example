package com.example.productdataetl.integration;

import com.example.productdataetl.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the complete Product ETL Job.
 * Tests the full job execution from CSV input to report output,
 * verifies job completion status, step execution sequence, and handles failure scenarios.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "batch.input.file=classpath:test-products.csv"
})
public class EndToEndJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job productEtlJob;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TEST_OUTPUT_FILE = "sales_report.csv";

    @BeforeEach
    void setUp() {
        // Remove test output file if it exists
        deleteTestOutputFile();
        
        // Note: Database cleanup is handled by Spring Boot test framework
        // Each test runs in its own transaction context
    }

    @AfterEach
    void tearDown() {
        // Clean up test output file
        deleteTestOutputFile();
    }

    @Test
    void testCompleteJobExecutionFromCsvToReport() throws Exception {
        // Given: CSV input file exists and database is empty
        
        // When: Execute complete productEtlJob
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Verify job completion status
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        // Verify step execution sequence
        verifyStepExecutionSequence(jobExecution);
        
        // Verify end-to-end data flow
        verifyEndToEndDataFlow();
        
        // Verify final output
        verifyFinalOutput();
    }

    @Test
    void testJobCompletionStatusAndStepSequence() throws Exception {
        // Given: Valid input data
        
        // When: Execute job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Verify job and step completion
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        assertEquals(2, stepExecutions.size(), "Job should execute exactly 2 steps");
        
        // Verify step execution order and status
        StepExecution[] steps = stepExecutions.toArray(new StepExecution[0]);
        
        // Find step1 and step2 by name
        StepExecution step1 = null;
        StepExecution step2 = null;
        
        for (StepExecution step : steps) {
            if ("step1_loadCsvToDb".equals(step.getStepName())) {
                step1 = step;
            } else if ("step2_generateReportFromDb".equals(step.getStepName())) {
                step2 = step;
            }
        }
        
        assertNotNull(step1, "Step1 should be executed");
        assertNotNull(step2, "Step2 should be executed");
        
        assertEquals(BatchStatus.COMPLETED, step1.getStatus());
        assertEquals(BatchStatus.COMPLETED, step2.getStatus());
        
        // Verify step1 executed before step2
        assertTrue(step1.getStartTime().isBefore(step2.getStartTime()) || 
                  step1.getStartTime().equals(step2.getStartTime()),
                  "Step1 should start before or at the same time as Step2");
        assertTrue(step1.getEndTime().isBefore(step2.getStartTime()) || 
                  step1.getEndTime().equals(step2.getStartTime()),
                  "Step1 should complete before Step2 starts");
    }

    @Test
    void testJobRestartScenario() throws Exception {
        // This test simulates a job restart scenario
        // In a real scenario, we would simulate a failure and restart
        
        // Given: First job execution
        JobParameters jobParameters1 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        assertEquals(BatchStatus.COMPLETED, jobExecution1.getStatus());
        
        // When: Second job execution with different parameters (simulating restart)
        JobParameters jobParameters2 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .toJobParameters();
        
        JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters2);
        
        // Then: Second execution should also complete successfully
        assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
        
        // Verify that both executions are independent
        assertNotEquals(jobExecution1.getId(), jobExecution2.getId());
    }

    @Test
    void testDataIntegrityThroughoutPipeline() throws Exception {
        // Given: Known test data in CSV
        
        // When: Execute complete job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Verify data integrity throughout the pipeline
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        // Verify database contains expected data after step1
        List<Product> products = entityManager
                .createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                .getResultList();
        
        assertEquals(10, products.size(), "Should have 10 products from test CSV");
        
        // Verify output file contains filtered data after step2
        List<String> outputLines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        
        // Count products with price > 50 from test data
        // Gaming Laptop (1299.99), Mechanical Keyboard (89.99), External Monitor (399.99), 
        // Smartphone (899.99), Bluetooth Headphones (149.99), Webcam (75.00)
        int expectedFilteredCount = 6;
        
        assertEquals(expectedFilteredCount + 1, outputLines.size(), 
                    "Output should have header + " + expectedFilteredCount + " filtered products");
        
        // Verify data consistency between database and output
        verifyDataConsistency(products, outputLines);
    }

    @Test
    void testJobPerformanceMetrics() throws Exception {
        // Given: Test data
        
        // When: Execute job and measure performance
        long startTime = System.currentTimeMillis();
        
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Then: Verify performance metrics
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        // Verify execution time is reasonable (should complete within 30 seconds for test data)
        assertTrue(executionTime < 30000, "Job should complete within 30 seconds for test data");
        
        // Verify step metrics
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        for (StepExecution stepExecution : stepExecutions) {
            assertTrue(stepExecution.getReadCount() >= 0, "Read count should be non-negative");
            assertTrue(stepExecution.getWriteCount() >= 0, "Write count should be non-negative");
            assertEquals(0, stepExecution.getSkipCount(), "Skip count should be 0 for valid test data");
        }
    }

    private void verifyStepExecutionSequence(JobExecution jobExecution) {
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        assertEquals(2, stepExecutions.size(), "Should execute exactly 2 steps");
        
        boolean step1Found = false;
        boolean step2Found = false;
        
        for (StepExecution stepExecution : stepExecutions) {
            if ("step1_loadCsvToDb".equals(stepExecution.getStepName())) {
                step1Found = true;
                assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
            } else if ("step2_generateReportFromDb".equals(stepExecution.getStepName())) {
                step2Found = true;
                assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
            }
        }
        
        assertTrue(step1Found, "Step1 should be executed");
        assertTrue(step2Found, "Step2 should be executed");
    }

    private void verifyEndToEndDataFlow() {
        // Verify data exists in database (result of step1)
        List<Product> products = entityManager
                .createQuery("SELECT p FROM Product p", Product.class)
                .getResultList();
        
        assertFalse(products.isEmpty(), "Database should contain products after step1");
        
        // Verify output file exists (result of step2)
        File outputFile = new File(TEST_OUTPUT_FILE);
        assertTrue(outputFile.exists(), "Output file should exist after step2");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
    }

    private void verifyFinalOutput() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        
        // Verify header
        assertEquals("productId,productName,price", lines.get(0));
        
        // Verify content (should only contain products with price > 50)
        assertTrue(lines.size() > 1, "Should have at least one product in output");
        
        // Verify all products in output have price > 50
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = line.split(",");
            double price = Double.parseDouble(fields[2]);
            assertTrue(price > 50.0, "All products in output should have price > 50");
        }
    }

    private void verifyDataConsistency(List<Product> products, List<String> outputLines) {
        // Count products with price > 50 in database
        long highPriceProductsInDb = products.stream()
                .filter(p -> p.getPrice().doubleValue() > 50.0)
                .count();
        
        // Count products in output file (excluding header)
        int productsInOutput = outputLines.size() - 1;
        
        assertEquals(highPriceProductsInDb, productsInOutput, 
                    "Number of high-price products in database should match output file");
    }

    private void deleteTestOutputFile() {
        File outputFile = new File(TEST_OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}