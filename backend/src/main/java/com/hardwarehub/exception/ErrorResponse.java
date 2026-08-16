package com.hardwarehub.exception;

/**
 * The one shape every error response takes, so API clients can rely on it
 * instead of parsing Spring's default (and inconsistent) error bodies.
 */
public record ErrorResponse(int status, String error, String message) {
}
