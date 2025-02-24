package com.example.weathersdk.exception;


public class JsonParsingException extends WeatherSdkException {

    public JsonParsingException(String message) {
        super(message);
    }

    public JsonParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}