package com.example.weathersdk.exception;


public class NetworkException extends WeatherSdkException {

    public NetworkException(String message, Throwable cause) {
        super(message, 0, null, cause);
    }
}

