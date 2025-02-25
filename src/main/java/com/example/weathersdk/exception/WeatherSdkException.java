package com.example.weathersdk.exception;

import lombok.Getter;

/**
 * Custom exception for the Weather SDK.
 * Used to indicate errors that occur while interacting with the weather API.
 */
@Getter
public class WeatherSdkException extends RuntimeException {

    /**
     * HTTP status code returned by the API, if applicable.
     */
    private final int statusCode;

    /**
     * Detailed error message from the API response, if available.
     */
    private final String errorDetails;

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public WeatherSdkException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorDetails = null;
    }

    /**
     * Constructs a new exception with the specified message, status code, and error details.
     *
     * @param message      the detail message
     * @param statusCode   the HTTP status code from the API response
     * @param errorDetails additional error details from the API response
     */
    public WeatherSdkException(String message, int statusCode, String errorDetails) {
        super(message);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    /**
     * Constructs a new exception with the specified message, status code, error details, and cause.
     *
     * @param message      the detail message
     * @param statusCode   the HTTP status code from the API response
     * @param errorDetails additional error details from the API response
     * @param cause        the cause of the exception
     */
    public WeatherSdkException(String message, int statusCode, String errorDetails, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public WeatherSdkException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorDetails = null;
    }
}
