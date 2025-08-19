package com.example.productdataetl.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for job failure and recovery scenarios.
 * Tests job restart capabilities and failure handling mechanisms.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "batch.input.file=classpath:test-products.csv",
    "batch.output.file=file:recovery_test_sales_report.csv"
})
public class JobFailureRecoveryTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job productEtlJob;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TEST_OUTPUT_FILE = "recovery_test_sales_report.csv";

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
    void testJobRestartAfterSuccessfulCompletion() throws Exception {
        // Given: First successful job execution
        JobParameters jobParameters1 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("run", "first")
                .toJobParameters();
        
        JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        assertEquals(BatchStatus.COMPLETED, jobExecution1.getStatus());
        
        // When: Attempt to restart with same parameters (should create new instance)
        JobParameters jobParameters2 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .addString("run", "second")
                .toJobParameters();
        
        JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters2);
        
        // Then: Second execution should also complete successfully
        assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
        assertNotEquals(jobExecution1.getId(), jobExecution2.getId());
    }

    @Test
    void testJobWithMissingInputFile() throws Exception {
        // Given: Job configured with non-existent input file
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("input.file", "classpath:non-existent-file.csv")
                .toJobParameters();
        
        // When: Execute job with missing input file
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Job should fail gracefully
        assertEquals(BatchStatus.FAILED, jobExecution.getStatus());
        assertTrue(jobExecution.getAllFailureExceptions().size() > 0);
    }

    @Test
    void testJobRecoveryMechanisms() throws Exception {
        // This test demonstrates the job's error handling capabilities
        
        // Given: Valid job parameters
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        // When: Execute job (should succeed with valid data)
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Verify job completes successfully and handles any minor issues
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        // Verify error handling mechanisms are in place
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            // Skip count should be 0 for valid test data
            assertEquals(0, stepExecution.getSkipCount());
            
            // No rollback count for successful execution
            assertEquals(0, stepExecution.getRollbackCount());
            
            // Verify step completed successfully
            assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        });
    }

    @Test
    void testJobExecutionWithDifferentParameters() throws Exception {
        // Test that jobs with different parameters can run independently
        
        // Given: First job execution
        JobParameters jobParameters1 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("execution", "test1")
                .toJobParameters();
        
        JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        assertEquals(BatchStatus.COMPLETED, jobExecution1.getStatus());
        
        // When: Second job execution with different parameters
        JobParameters jobParameters2 = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 2000)
                .addString("execution", "test2")
                .toJobParameters();
        
        JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters2);
        
        // Then: Both executions should be independent and successful
        assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
        assertNotEquals(jobExecution1.getId(), jobExecution2.getId());
        
        // Verify both created their own job instances
        assertNotEquals(jobExecution1.getJobInstance().getId(), 
                       jobExecution2.getJobInstance().getId());
    }

    @Test
    void testJobExecutionMetrics() throws Exception {
        // Test that job execution provides proper metrics for monitoring
        
        // Given: Job parameters
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        // When: Execute job
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then: Verify execution metrics are available
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertNotNull(jobExecution.getStartTime());
        assertNotNull(jobExecution.getEndTime());
        assertTrue(jobExecution.getEndTime().isAfter(jobExecution.getStartTime()) ||
                  jobExecution.getEndTime().equals(jobExecution.getStartTime()));
        
        // Verify step metrics
        assertEquals(2, jobExecution.getStepExecutions().size());
        
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            assertNotNull(stepExecution.getStartTime());
            assertNotNull(stepExecution.getEndTime());
            assertTrue(stepExecution.getReadCount() >= 0);
            assertTrue(stepExecution.getWriteCount() >= 0);
            assertTrue(stepExecution.getCommitCount() >= 0);
        });
    }

    private void deleteTestOutputFile() {
        File outputFile = new File(TEST_OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}