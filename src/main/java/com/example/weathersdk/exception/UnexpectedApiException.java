package com.example.weathersdk.exception;

/**
 * Exception thrown when an unexpected error occurs while interacting with the weather API.
 * This typically indicates an issue that is not covered by specific API error handling.
 */
public class UnexpectedApiException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public UnexpectedApiException(String message, Throwable cause) {
        super(message, 0, null, cause);
    }
}
