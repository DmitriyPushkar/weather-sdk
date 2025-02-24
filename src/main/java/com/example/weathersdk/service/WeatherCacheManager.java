package com.example.weathersdk.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;
import java.util.Set;

// Manages caching of weather data with a limit on stored cities and expiration time
class WeatherCacheManager {
    private static final int MAX_CITIES = 10;
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(10);
    private final Cache<String, String> cache;


    private WeatherCacheManager(Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(MAX_CITIES)
                .expireAfterWrite(EXPIRATION_TIME)
                .ticker(ticker)
                .build();
    }

    public String getCachedData(final String cityName) {
        return null;
    }

    public void updateCache(final String cityName, final String data) {
    }

    private void removeOldestEntry() {
    }

    public Set<String> getCachedCities() {
        return null;
    }

    public void clearCache() {
    }
}