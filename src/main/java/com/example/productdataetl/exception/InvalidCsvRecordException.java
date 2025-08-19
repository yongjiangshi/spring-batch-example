package com.example.productdataetl.exception;

/**
 * Exception thrown when a CSV record cannot be parsed or contains invalid data.
 * This is a skippable exception that allows the batch job to continue processing
 * other records while logging the problematic ones.
 */
public class InvalidCsvRecordException extends ProductDataException {
    
    private final String csvRecord;
    private final int lineNumber;
    
    public InvalidCsvRecordException(String message, String csvRecord, int lineNumber) {
        super(message);
        this.csvRecord = csvRecord;
        this.lineNumber = lineNumber;
    }
    
    public InvalidCsvRecordException(String message, String csvRecord, int lineNumber, Throwable cause) {
        super(message, cause);
        this.csvRecord = csvRecord;
        this.lineNumber = lineNumber;
    }
    
    public String getCsvRecord() {
        return csvRecord;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    @Override
    public String getMessage() {
        return String.format("Invalid CSV record at line %d: %s. Error: %s", 
                lineNumber, csvRecord, super.getMessage());
    }
}