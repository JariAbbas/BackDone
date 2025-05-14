package org.acme.exception;

/**
 * Exception thrown when a requested loan or its necessary related data cannot be found.
 */
public class LoanNotFoundException extends Exception {
    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}