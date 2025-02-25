package com.example.weathersdk.exception;

/**
 * Exception thrown when the provided API key has been blocked by the OpenWeather API.
 * This may occur due to excessive requests, policy violations, or other restrictions.
 */
public class ApiKeyBlockedException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public ApiKeyBlockedException(String message) {
        super(message);
    }
}
