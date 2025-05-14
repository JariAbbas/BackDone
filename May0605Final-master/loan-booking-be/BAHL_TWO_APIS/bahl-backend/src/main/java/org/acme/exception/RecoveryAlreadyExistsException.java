package org.acme.exception;

/**
 * Exception thrown when attempting to perform a recovery action on a loan
 * for which a recovery record already exists.
 */
public class RecoveryAlreadyExistsException extends Exception {
    public RecoveryAlreadyExistsException(String message) {
        super(message);
    }

    public RecoveryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}