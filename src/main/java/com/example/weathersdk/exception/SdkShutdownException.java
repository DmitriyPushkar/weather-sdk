package com.example.weathersdk.exception;

/**
 * Exception thrown when an operation is attempted on the SDK after it has been shut down.
 * This typically indicates an improper usage of the SDK lifecycle.
 */
public class SdkShutdownException extends WeatherSdkException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public SdkShutdownException(String message) {
        super(message);
    }
}
