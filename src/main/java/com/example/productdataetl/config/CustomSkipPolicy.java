package com.example.productdataetl.config;

import com.example.productdataetl.exception.InvalidCsvRecordException;
import com.example.productdataetl.exception.TransientDatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataAccessException;

/**
 * Custom skip policy that determines which exceptions should be skipped
 * and which should cause the job to fail immediately.
 */
public class CustomSkipPolicy implements SkipPolicy {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomSkipPolicy.class);
    private final int skipLimit;
    
    public CustomSkipPolicy(int skipLimit) {
        this.skipLimit = skipLimit;
    }
    
    @Override
    public boolean shouldSkip(Throwable exception, long skipCount) throws SkipLimitExceededException {
        
        if (skipCount >= skipLimit) {
            logger.error("Skip limit exceeded. Current skip count: {}, limit: {}", skipCount, skipLimit);
            return false;
        }
        
        // Skip CSV parsing errors and invalid record exceptions
        if (exception instanceof FlatFileParseException || 
            exception instanceof InvalidCsvRecordException) {
            logger.warn("Skipping invalid record (skip count: {}): {}", skipCount + 1, exception.getMessage());
            return true;
        }
        
        // Skip certain data access exceptions that are not transient
        if (exception instanceof DataAccessException) {
            String message = exception.getMessage();
            if (message != null && (message.contains("constraint violation") || 
                                  message.contains("duplicate key"))) {
                logger.warn("Skipping data constraint violation (skip count: {}): {}", skipCount + 1, exception.getMessage());
                return true;
            }
        }
        
        // Don't skip transient database exceptions - these should be retried
        if (exception instanceof TransientDatabaseException) {
            logger.debug("Not skipping transient database exception - will retry: {}", exception.getMessage());
            return false;
        }
        
        // Don't skip other exceptions
        logger.error("Not skipping exception: {}", exception.getMessage());
        return false;
    }
}