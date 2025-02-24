package com.example.weathersdk.exception;

import lombok.Getter;

// Custom exception for Weather SDK errors
@Getter
public class WeatherSdkException extends RuntimeException {
    private final int statusCode;

    private final String errorDetails;

    public WeatherSdkException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorDetails = null;
    }

    public WeatherSdkException(String message, int statusCode, String errorDetails) {
        super(message);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    public WeatherSdkException(String message, int statusCode, String errorDetails, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    public WeatherSdkException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorDetails = null;
    }
}

