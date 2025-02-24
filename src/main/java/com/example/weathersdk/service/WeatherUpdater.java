package com.example.weathersdk.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Handles periodic weather updates in polling mode
public class WeatherUpdater {
    private final WeatherCacheManager cacheManager;
    private final OpenWeatherApiClient apiClient;
    private final ScheduledExecutorService scheduler;

    public WeatherUpdater(WeatherCacheManager cacheManager, OpenWeatherApiClient apiClient, int interval) {
        this.cacheManager = cacheManager;
        this.apiClient = apiClient;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::updateWeather, 0, interval, TimeUnit.SECONDS);
    }

    private void updateWeather() {
    }

    public void stop() {
        scheduler.shutdown();
    }
}
