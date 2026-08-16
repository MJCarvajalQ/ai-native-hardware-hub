package com.hardwarehub.exception;

/**
 * Thrown when an admin tries to create an account with an email that's
 * already registered. Mapped to 409 in GlobalExceptionHandler.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("email already in use: " + email);
    }
}
