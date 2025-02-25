package com.example.weathersdk.exception;

/**
 * Exception thrown when an invalid city name is provided.
 * This may occur if the city name is null, empty, or improperly formatted.
 */
public class InvalidCityException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public InvalidCityException(String message) {
        super(message);
    }
}
