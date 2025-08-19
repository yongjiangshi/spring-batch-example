package com.example.productdataetl.integration;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for integration tests.
 * Provides necessary test utilities for Spring Batch integration testing.
 */
@TestConfiguration
public class IntegrationTestConfiguration {

    /**
     * Provides JobLauncherTestUtils for launching jobs and steps in tests.
     * 
     * @return JobLauncherTestUtils instance
     */
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }

    /**
     * Provides JobRepositoryTestUtils for managing job repository in tests.
     * 
     * @return JobRepositoryTestUtils instance
     */
    @Bean
    public JobRepositoryTestUtils jobRepositoryTestUtils() {
        return new JobRepositoryTestUtils();
    }
}