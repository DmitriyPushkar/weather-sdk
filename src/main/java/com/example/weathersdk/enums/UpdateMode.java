package com.example.weathersdk.enums;

/**
 * Modes of updating weather data in the SDK.
 */
public enum UpdateMode {
    /**
     * Periodically refreshes weather data for stored cities.
     */
    POLLING,

    /**
     * Updates weather data only upon user request.
     */
    ON_DEMAND
}