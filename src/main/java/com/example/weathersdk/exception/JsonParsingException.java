package com.example.weathersdk.exception;

/**
 * Exception thrown when an error occurs while parsing JSON data from the weather API response.
 * This may indicate invalid JSON structure or unexpected response format.
 */
public class JsonParsingException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public JsonParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
