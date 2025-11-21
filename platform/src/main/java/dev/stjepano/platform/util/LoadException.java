package dev.stjepano.platform.util;

/// Thrown when you fail to load "something".
public class LoadException extends RuntimeException {
    public LoadException(String message) {
        super(message);
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
