package com.hardwarehub.exception;

/**
 * Thrown when the Claude call fails or returns something the parser can't
 * make sense of. Maps to 502 — the caller's request was fine, an upstream
 * dependency wasn't.
 */
public class SearchUnavailableException extends RuntimeException {

    public SearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchUnavailableException(String message) {
        super(message);
    }
}
