package com.example.weathersdk.exception;


public class UnexpectedApiException extends WeatherSdkException {

    public UnexpectedApiException(String message, int statusCode, String errorDetails) {
        super(message, statusCode, errorDetails);
    }

    public UnexpectedApiException(String message, Throwable cause) {
        super(message, 0, null, cause);
    }
}