package org.acme.exception;

public class UnauthorizedLoanException extends Exception {
    public UnauthorizedLoanException(String message) {
        super(message);
    }
}