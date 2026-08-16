package com.hardwarehub.exception;

/**
 * Thrown whenever a requested rent/return/repair-toggle would move a
 * Hardware into an impossible state (e.g. renting something already in
 * use, or returning something nobody has). Mapped to 409 in
 * GlobalExceptionHandler (Block F5).
 */
public class IllegalHardwareStateException extends RuntimeException {

    public IllegalHardwareStateException(String message) {
        super(message);
    }
}
