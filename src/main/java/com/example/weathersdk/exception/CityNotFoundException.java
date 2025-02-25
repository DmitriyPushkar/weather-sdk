package com.example.weathersdk.exception;

/**
 * Exception thrown when a requested city is not found in the weather API response.
 * This may occur if the city name is misspelled, does not exist, or is not available in the API database.
 */
public class CityNotFoundException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public CityNotFoundException(String message) {
        super(message);
    }
}
