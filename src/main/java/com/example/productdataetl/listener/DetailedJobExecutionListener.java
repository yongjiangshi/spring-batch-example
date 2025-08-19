package com.example.productdataetl.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

/**
 * Enhanced job execution listener that provides comprehensive logging
 * and monitoring of overall job execution, including aggregated statistics
 * from all steps and detailed error reporting.
 */
public class DetailedJobExecutionListener implements JobExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DetailedJobExecutionListener.class);
    private LocalDateTime jobStartTime;
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobStartTime = LocalDateTime.now();
        logger.info("========================================");
        logger.info("=== Starting Job: {} ===", jobExecution.getJobInstance().getJobName());
        logger.info("========================================");
        logger.info("Job ID: {}", jobExecution.getJobId());
        logger.info("Job instance ID: {}", jobExecution.getJobInstance().getInstanceId());
        logger.info("Job parameters: {}", jobExecution.getJobParameters());
        logger.info("Job start time: {}", jobStartTime);
        logger.info("Job version: {}", jobExecution.getVersion());
        
        // Log system information
        logSystemInformation();
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime jobEndTime = LocalDateTime.now();
        Duration totalDuration = Duration.between(jobStartTime, jobEndTime);
        
        logger.info("========================================");
        logger.info("=== Completed Job: {} ===", jobExecution.getJobInstance().getJobName());
        logger.info("========================================");
        logger.info("Job end time: {}", jobEndTime);
        logger.info("Total job duration: {} seconds", totalDuration.getSeconds());
        logger.info("Job status: {}", jobExecution.getStatus());
        logger.info("Job exit status: {}", jobExecution.getExitStatus());
        
        // Log aggregated statistics from all steps
        logAggregatedStatistics(jobExecution);
        
        // Log step-by-step summary
        logStepSummary(jobExecution);
        
        // Log any job-level failures
        if (jobExecution.getAllFailureExceptions() != null && !jobExecution.getAllFailureExceptions().isEmpty()) {
            logger.error("Job failed with {} exceptions:", jobExecution.getAllFailureExceptions().size());
            for (Throwable exception : jobExecution.getAllFailureExceptions()) {
                logger.error("Job failure exception: {}", exception.getMessage(), exception);
            }
        }
        
        // Log final status and recommendations
        logFinalStatusAndRecommendations(jobExecution, totalDuration);
        
        logger.info("========================================");
    }
    
    private void logSystemInformation() {
        Runtime runtime = Runtime.getRuntime();
        logger.info("--- System Information ---");
        logger.info("Available processors: {}", runtime.availableProcessors());
        logger.info("Max memory: {} MB", runtime.maxMemory() / 1024 / 1024);
        logger.info("Total memory: {} MB", runtime.totalMemory() / 1024 / 1024);
        logger.info("Free memory: {} MB", runtime.freeMemory() / 1024 / 1024);
        logger.info("Java version: {}", System.getProperty("java.version"));
    }
    
    private void logAggregatedStatistics(JobExecution jobExecution) {
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        
        int totalRead = 0;
        int totalWritten = 0;
        int totalSkipped = 0;
        int totalRollbacks = 0;
        int totalCommits = 0;
        
        for (StepExecution stepExecution : stepExecutions) {
            totalRead += stepExecution.getReadCount();
            totalWritten += stepExecution.getWriteCount();
            totalSkipped += stepExecution.getSkipCount();
            totalRollbacks += stepExecution.getRollbackCount();
            totalCommits += stepExecution.getCommitCount();
        }
        
        logger.info("--- Aggregated Job Statistics ---");
        logger.info("Total items read: {}", totalRead);
        logger.info("Total items written: {}", totalWritten);
        logger.info("Total items skipped: {}", totalSkipped);
        logger.info("Total rollbacks: {}", totalRollbacks);
        logger.info("Total commits: {}", totalCommits);
        logger.info("Number of steps executed: {}", stepExecutions.size());
        
        if (totalRead > 0) {
            double overallSuccessRate = ((double) (totalRead - totalSkipped) / totalRead) * 100;
            logger.info("Overall success rate: {:.2f}%", overallSuccessRate);
        }
    }
    
    private void logStepSummary(JobExecution jobExecution) {
        logger.info("--- Step Execution Summary ---");
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            logger.info("Step '{}': {} - Read: {}, Written: {}, Skipped: {}", 
                    stepExecution.getStepName(),
                    stepExecution.getStatus(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount());
            
            if (stepExecution.getFailureExceptions() != null && !stepExecution.getFailureExceptions().isEmpty()) {
                logger.error("Step '{}' had {} failure(s)", 
                        stepExecution.getStepName(), 
                        stepExecution.getFailureExceptions().size());
            }
        }
    }
    
    private void logFinalStatusAndRecommendations(JobExecution jobExecution, Duration totalDuration) {
        logger.info("--- Final Status and Recommendations ---");
        
        if (jobExecution.getStatus().isUnsuccessful()) {
            logger.error("Job FAILED. Check the error logs above for details.");
            logger.error("Exit description: {}", jobExecution.getExitStatus().getExitDescription());
            
            // Provide recommendations based on failure type
            if (jobExecution.getExitStatus().getExitCode().contains("FAILED")) {
                logger.error("Recommendation: Review the step configurations and input data for errors.");
            }
        } else {
            logger.info("Job COMPLETED SUCCESSFULLY!");
            
            // Performance recommendations
            if (totalDuration.getSeconds() > 300) { // More than 5 minutes
                logger.info("Recommendation: Job took {} seconds. Consider optimizing chunk sizes or adding parallel processing.", 
                        totalDuration.getSeconds());
            }
            
            // Check for high skip rates
            int totalSkipped = jobExecution.getStepExecutions().stream()
                    .mapToInt(step -> (int) step.getSkipCount())
                    .sum();
            int totalRead = jobExecution.getStepExecutions().stream()
                    .mapToInt(step -> (int) step.getReadCount())
                    .sum();
            
            if (totalRead > 0 && (double) totalSkipped / totalRead > 0.1) { // More than 10% skipped
                logger.warn("Recommendation: High skip rate ({:.1f}%). Review input data quality.", 
                        ((double) totalSkipped / totalRead) * 100);
            }
        }
    }
}