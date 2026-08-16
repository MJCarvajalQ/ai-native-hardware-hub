package com.hardwarehub.exception;

/**
 * Thrown when a hardware id doesn't exist. Mapped to 404 in
 * GlobalExceptionHandler (Block F5).
 */
public class HardwareNotFoundException extends RuntimeException {

    public HardwareNotFoundException(Long id) {
        super("hardware not found: " + id);
    }
}
