package dev.stjepano.platform.util;

/// Thrown when you try to read from resource but resource was not found.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
