package com.example.weathersdk.exception;

/**
 * Exception thrown when a network-related issue occurs while interacting with the weather API.
 * This may include connection timeouts, unreachable servers, or other network failures.
 */
public class NetworkException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NetworkException(String message, Throwable cause) {
        super(message, 0, null, cause);
    }
}
