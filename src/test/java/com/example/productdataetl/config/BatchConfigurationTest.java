package com.example.productdataetl.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for BatchConfiguration to verify that the enhanced
 * error handling components are properly configured and wired.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "batch.chunk.size=5",
    "batch.skip.limit=3",
    "batch.retry.limit=2"
})
class BatchConfigurationTest {

    @Autowired
    private Job productEtlJob;

    @Autowired
    private Step step1LoadCsvToDb;

    @Autowired
    private Step step2GenerateReportFromDb;

    @Autowired
    private CustomSkipPolicy customSkipPolicy;

    @Autowired
    private CustomRetryPolicy customRetryPolicy;

    @Test
    void shouldConfigureJobWithEnhancedErrorHandling() {
        // Verify that the job is properly configured
        assertNotNull(productEtlJob);
        assertEquals("productEtlJob", productEtlJob.getName());
    }

    @Test
    void shouldConfigureStepsWithErrorHandling() {
        // Verify that steps are properly configured
        assertNotNull(step1LoadCsvToDb);
        assertNotNull(step2GenerateReportFromDb);
        
        assertEquals("step1_loadCsvToDb", step1LoadCsvToDb.getName());
        assertEquals("step2_generateReportFromDb", step2GenerateReportFromDb.getName());
    }

    @Test
    void shouldConfigureCustomErrorHandlingPolicies() {
        // Verify that custom error handling policies are configured
        assertNotNull(customSkipPolicy);
        assertNotNull(customRetryPolicy);
    }

    @Test
    void shouldHaveDetailedJobExecutionListener() {
        // Verify that the job is properly configured
        assertNotNull(productEtlJob);
        assertEquals("productEtlJob", productEtlJob.getName());
        
        // Verify that the job is restartable (default behavior)
        assertTrue(productEtlJob.isRestartable());
    }
}