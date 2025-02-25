package com.example.weathersdk.exception;

/**
 * Exception thrown when an invalid API key is provided for the OpenWeather API.
 * This may occur if the API key is null, empty, or rejected by the API.
 */
public class InvalidApiKeyException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public InvalidApiKeyException(String message) {
        super(message);
    }
}
