package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;

import java.util.List;

// Facade for managing weather data retrieval, caching and automatic weather updates in polling mode
public class WeatherSdkImpl implements WeatherSdk {
    @Override
    public String getWeather(String cityName) {
        return "";
    }

    @Override
    public void updateWeather(String cityName) {

    }

    @Override
    public void stopPolling() {

    }

    @Override
    public boolean isPollingEnabled() {
        return false;
    }

    @Override
    public List<String> getCachedCities() {
        return List.of();
    }

    @Override
    public void clearCache() {

    }
}
