package com.example.productdataetl.exception;

/**
 * Exception for transient database errors that can be retried.
 * Examples include connection timeouts, deadlocks, or temporary unavailability.
 */
public class TransientDatabaseException extends ProductDataException {
    
    public TransientDatabaseException(String message) {
        super(message);
    }
    
    public TransientDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}