package com.hardwarehub.exception;

/**
 * Thrown for a missing email or a wrong password. Deliberately does not say
 * which one, so login responses can't be used to check whether an email is
 * registered. Mapped to 401 in GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("invalid email or password");
    }
}
