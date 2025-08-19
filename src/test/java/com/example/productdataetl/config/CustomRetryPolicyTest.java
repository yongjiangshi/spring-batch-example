package com.example.productdataetl.config;

import com.example.productdataetl.exception.InvalidCsvRecordException;
import com.example.productdataetl.exception.TransientDatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryContext;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomRetryPolicy to verify correct retry behavior
 * for different types of exceptions.
 */
class CustomRetryPolicyTest {

    private CustomRetryPolicy retryPolicy;
    private static final int MAX_ATTEMPTS = 3;

    @BeforeEach
    void setUp() {
        retryPolicy = new CustomRetryPolicy(MAX_ATTEMPTS);
    }

    @Test
    void shouldRetryTransientDatabaseException() {
        TransientDatabaseException exception = new TransientDatabaseException("Connection timeout");
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertFalse(retryPolicy.canRetry(context)); // Should exceed limit
    }

    @Test
    void shouldRetryTransientDataAccessException() {
        TransientDataAccessException exception = new TransientDataAccessException("Temporary failure") {};
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertTrue(retryPolicy.canRetry(context));
    }

    @Test
    void shouldRetryDataAccessResourceFailureException() {
        DataAccessResourceFailureException exception = new DataAccessResourceFailureException("Resource unavailable");
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertTrue(retryPolicy.canRetry(context));
    }

    @Test
    void shouldRetrySQLExceptionWithFewerAttempts() {
        SQLException exception = new SQLException("Database error");
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertTrue(retryPolicy.canRetry(context));
        
        retryPolicy.registerThrowable(context, exception);
        assertFalse(retryPolicy.canRetry(context)); // Should exceed limit (maxAttempts - 1)
    }

    @Test
    void shouldNotRetryInvalidCsvRecordException() {
        InvalidCsvRecordException exception = new InvalidCsvRecordException("Invalid record", "bad,data", 5);
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context)); // First attempt
        
        retryPolicy.registerThrowable(context, exception);
        assertFalse(retryPolicy.canRetry(context)); // No retry for this exception type
    }

    @Test
    void shouldNotRetryUnknownException() {
        RuntimeException exception = new RuntimeException("Unknown error");
        RetryContext context = retryPolicy.open(null);
        
        assertTrue(retryPolicy.canRetry(context)); // First attempt
        
        retryPolicy.registerThrowable(context, exception);
        assertFalse(retryPolicy.canRetry(context)); // No retry for unknown exceptions
    }

    @Test
    void shouldHandleContextLifecycle() {
        RetryContext context = retryPolicy.open(null);
        assertNotNull(context);
        
        // Should not throw exception
        retryPolicy.close(context);
    }
}