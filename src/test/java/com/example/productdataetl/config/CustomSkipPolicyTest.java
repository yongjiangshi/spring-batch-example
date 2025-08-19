package com.example.productdataetl.config;

import com.example.productdataetl.exception.InvalidCsvRecordException;
import com.example.productdataetl.exception.TransientDatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomSkipPolicy to verify correct skip behavior
 * for different types of exceptions.
 */
class CustomSkipPolicyTest {

    private CustomSkipPolicy skipPolicy;
    private static final int SKIP_LIMIT = 3;

    @BeforeEach
    void setUp() {
        skipPolicy = new CustomSkipPolicy(SKIP_LIMIT);
    }

    @Test
    void shouldSkipFlatFileParseException() throws SkipLimitExceededException {
        FlatFileParseException exception = new FlatFileParseException("Parse error", "invalid line");
        
        assertTrue(skipPolicy.shouldSkip(exception, 0));
        assertTrue(skipPolicy.shouldSkip(exception, 1));
        assertTrue(skipPolicy.shouldSkip(exception, 2));
    }

    @Test
    void shouldSkipInvalidCsvRecordException() throws SkipLimitExceededException {
        InvalidCsvRecordException exception = new InvalidCsvRecordException("Invalid record", "bad,data", 5);
        
        assertTrue(skipPolicy.shouldSkip(exception, 0));
        assertTrue(skipPolicy.shouldSkip(exception, 1));
    }

    @Test
    void shouldSkipDataIntegrityViolationException() throws SkipLimitExceededException {
        DataAccessException exception = new DataIntegrityViolationException("constraint violation");
        
        assertTrue(skipPolicy.shouldSkip(exception, 0));
    }

    @Test
    void shouldNotSkipTransientDatabaseException() throws SkipLimitExceededException {
        TransientDatabaseException exception = new TransientDatabaseException("Connection timeout");
        
        assertFalse(skipPolicy.shouldSkip(exception, 0));
    }

    @Test
    void shouldNotSkipWhenSkipLimitExceeded() throws SkipLimitExceededException {
        FlatFileParseException exception = new FlatFileParseException("Parse error", "invalid line");
        
        assertFalse(skipPolicy.shouldSkip(exception, SKIP_LIMIT));
        assertFalse(skipPolicy.shouldSkip(exception, SKIP_LIMIT + 1));
    }

    @Test
    void shouldNotSkipUnknownException() throws SkipLimitExceededException {
        RuntimeException exception = new RuntimeException("Unknown error");
        
        assertFalse(skipPolicy.shouldSkip(exception, 0));
    }

    @Test
    void shouldSkipConstraintViolationInMessage() throws SkipLimitExceededException {
        DataAccessException exception = new DataAccessException("duplicate key constraint violation") {};
        
        assertTrue(skipPolicy.shouldSkip(exception, 0));
    }

    @Test
    void shouldNotSkipDataAccessExceptionWithoutConstraintViolation() throws SkipLimitExceededException {
        DataAccessException exception = new DataAccessException("connection failed") {};
        
        assertFalse(skipPolicy.shouldSkip(exception, 0));
    }
}