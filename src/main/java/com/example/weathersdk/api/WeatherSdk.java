package com.example.weathersdk.api;

import java.util.List;


public interface WeatherSdk {
    String getWeather(String cityName);

    void updateWeather(String cityName);

    void stopPolling();

    boolean isPollingEnabled();

    List<String> getCachedCities();

    void clearCache();
}