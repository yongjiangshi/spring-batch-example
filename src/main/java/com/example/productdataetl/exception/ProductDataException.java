package com.example.productdataetl.exception;

/**
 * Base exception class for product data processing errors.
 * Used to categorize different types of errors in the ETL pipeline.
 */
public class ProductDataException extends Exception {
    
    public ProductDataException(String message) {
        super(message);
    }
    
    public ProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}