package com.example.productdataetl.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Enhanced step execution listener that provides detailed logging
 * and monitoring of step progress, performance, and error statistics.
 */
public class DetailedStepExecutionListener implements StepExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DetailedStepExecutionListener.class);
    private LocalDateTime stepStartTime;
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepStartTime = LocalDateTime.now();
        logger.info("=== Starting Step: {} ===", stepExecution.getStepName());
        logger.info("Step parameters: {}", stepExecution.getJobParameters());
        logger.info("Step start time: {}", stepStartTime);
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LocalDateTime stepEndTime = LocalDateTime.now();
        Duration duration = Duration.between(stepStartTime, stepEndTime);
        
        logger.info("=== Completed Step: {} ===", stepExecution.getStepName());
        logger.info("Step end time: {}", stepEndTime);
        logger.info("Step duration: {} seconds", duration.getSeconds());
        logger.info("Step status: {}", stepExecution.getStatus());
        logger.info("Step exit status: {}", stepExecution.getExitStatus());
        
        // Log detailed statistics
        logStepStatistics(stepExecution);
        
        // Log any failures
        if (stepExecution.getFailureExceptions() != null && !stepExecution.getFailureExceptions().isEmpty()) {
            logger.error("Step failed with {} exceptions:", stepExecution.getFailureExceptions().size());
            for (Throwable exception : stepExecution.getFailureExceptions()) {
                logger.error("Failure exception: {}", exception.getMessage(), exception);
            }
        }
        
        // Calculate and log performance metrics
        logPerformanceMetrics(stepExecution, duration);
        
        return stepExecution.getExitStatus();
    }
    
    private void logStepStatistics(StepExecution stepExecution) {
        logger.info("--- Step Statistics ---");
        logger.info("Items read: {}", stepExecution.getReadCount());
        logger.info("Items written: {}", stepExecution.getWriteCount());
        logger.info("Items skipped: {}", stepExecution.getSkipCount());
        logger.info("Items filtered: {}", stepExecution.getFilterCount());
        logger.info("Read skips: {}", stepExecution.getReadSkipCount());
        logger.info("Write skips: {}", stepExecution.getWriteSkipCount());
        logger.info("Process skips: {}", stepExecution.getProcessSkipCount());
        logger.info("Rollback count: {}", stepExecution.getRollbackCount());
        logger.info("Commit count: {}", stepExecution.getCommitCount());
        
        // Log skip details if any
        if (stepExecution.getSkipCount() > 0) {
            logger.warn("Step had {} skipped items. Check logs for details on skipped records.", 
                    stepExecution.getSkipCount());
        }
        
        // Log rollback details if any
        if (stepExecution.getRollbackCount() > 0) {
            logger.warn("Step had {} rollbacks. This may indicate transient errors or data issues.", 
                    stepExecution.getRollbackCount());
        }
    }
    
    private void logPerformanceMetrics(StepExecution stepExecution, Duration duration) {
        logger.info("--- Performance Metrics ---");
        
        long totalSeconds = duration.getSeconds();
        if (totalSeconds > 0) {
            double itemsPerSecond = (double) stepExecution.getReadCount() / totalSeconds;
            logger.info("Processing rate: {:.2f} items/second", itemsPerSecond);
            
            if (stepExecution.getWriteCount() > 0) {
                double writeRate = (double) stepExecution.getWriteCount() / totalSeconds;
                logger.info("Write rate: {:.2f} items/second", writeRate);
            }
        }
        
        // Calculate success rate
        int totalProcessed = (int) stepExecution.getReadCount();
        if (totalProcessed > 0) {
            double successRate = ((double) (totalProcessed - stepExecution.getSkipCount()) / totalProcessed) * 100;
            logger.info("Success rate: {:.2f}%", successRate);
        }
        
        logger.info("Memory usage: {} MB", 
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
    }
}