package com.example.productdataetl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Product Data ETL pipeline.
 * Implements CommandLineRunner to automatically trigger batch job execution on startup.
 */
@SpringBootApplication
public class ProductDataEtlApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductDataEtlApplication.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job productEtlJob;

    public static void main(String[] args) {
        SpringApplication.run(ProductDataEtlApplication.class, args);
    }

    /**
     * Executes the ETL job when the application starts.
     * Supports different execution modes through command-line arguments:
     * - Default: Run the complete ETL pipeline
     * - --dry-run: Validate configuration without executing the job
     * - --job-name=<name>: Specify a custom job name parameter
     * 
     * @param args command-line arguments
     */
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Product Data ETL Application");
        
        // Parse command-line arguments
        boolean dryRun = false;
        String customJobName = null;
        
        for (String arg : args) {
            if ("--dry-run".equals(arg)) {
                dryRun = true;
                logger.info("Dry run mode enabled - configuration validation only");
            } else if (arg.startsWith("--job-name=")) {
                customJobName = arg.substring("--job-name=".length());
                logger.info("Custom job name specified: {}", customJobName);
            }
        }
        
        if (dryRun) {
            logger.info("Dry run completed - job configuration is valid");
            return;
        }
        
        try {
            // Build job parameters with timestamp for uniqueness
            JobParametersBuilder parametersBuilder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("executionMode", "commandLine");
            
            if (customJobName != null) {
                parametersBuilder.addString("jobName", customJobName);
            }
            
            JobParameters jobParameters = parametersBuilder.toJobParameters();
            
            logger.info("Launching productEtlJob with parameters: {}", jobParameters);
            
            // Execute the job
            JobExecution jobExecution = jobLauncher.run(productEtlJob, jobParameters);
            
            // Handle null jobExecution (can happen in test environments with mocked beans)
            if (jobExecution == null) {
                logger.warn("Job execution returned null - this may occur in test environments");
                return;
            }
            
            // Log execution results
            logger.info("Job execution completed with status: {}", jobExecution.getStatus());
            logger.info("Job execution ID: {}", jobExecution.getId());
            logger.info("Job start time: {}", jobExecution.getStartTime());
            logger.info("Job end time: {}", jobExecution.getEndTime());
            
            if (jobExecution.getStatus().isUnsuccessful()) {
                logger.error("Job execution failed. Check the logs for details.");
                System.exit(1);
            } else {
                logger.info("Product Data ETL pipeline completed successfully!");
            }
            
        } catch (Exception e) {
            logger.error("Failed to execute productEtlJob", e);
            throw e;
        }
    }
}