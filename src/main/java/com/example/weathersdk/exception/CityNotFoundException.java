package com.example.weathersdk.exception;


public class CityNotFoundException extends WeatherSdkException {

    public CityNotFoundException(String message, String errorDetails) {
        super(message, 404, errorDetails);
    }

    public CityNotFoundException(String message) {
        super(message);
    }
}
