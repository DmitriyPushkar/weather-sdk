package com.example.weathersdk.exception;


public class InvalidApiKeyException extends WeatherSdkException {

    public InvalidApiKeyException(String message, String errorDetails) {
        super(message, 401, errorDetails);
    }

    public InvalidApiKeyException(String message) {
        super(message);
    }
}
