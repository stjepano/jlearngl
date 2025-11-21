package dev.stjepano.platform.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class JavaResources {

    /// Load resource into string.
    ///
    /// @param resourcePath the path to the resource
    /// @throws ResourceNotFoundException if resource not found
    /// @throws LoadException if failed to load resource
    public static String loadIntoString(String resourcePath) {
        try (InputStream inputStream = JavaResources.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("Resource " + resourcePath + " not found!");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new LoadException("Failed to load resource " + resourcePath, e);
        }
    }

    /// Load resource into byte array.
    ///
    /// @param resourcePath the path to the resource
    /// @throws ResourceNotFoundException if resource not found
    /// @throws LoadException if failed to load resource
    public static byte[] loadIntoByteArray(String resourcePath) {
        try (InputStream inputStream = JavaResources.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("Resource " + resourcePath + " not found!");
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new LoadException("Failed to load resource " + resourcePath, e);
        }
    }
}
