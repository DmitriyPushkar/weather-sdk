package com.example.weathersdk.exception;


public class CityNotFoundException extends WeatherSdkException {

    public CityNotFoundException(String message) {
        super(message);
    }
}
