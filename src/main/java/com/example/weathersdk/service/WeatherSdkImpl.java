package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.enums.UpdateMode;
import com.example.weathersdk.exception.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of the {@link WeatherSdk} interface.
 * Provides weather data retrieval with caching and optional polling mode.
 */
@Slf4j
class WeatherSdkImpl implements WeatherSdk {
    private final OpenWeatherApiClient apiClient;
    private final WeatherCacheManager cacheManager;
    private final ReentrantLock lock = new ReentrantLock();
    private WeatherUpdater weatherUpdater;

    @Getter
    private final UpdateMode mode;
    @Getter
    private final int pollingIntervalSeconds;
    private volatile boolean isShutdown = false;

    /**
     * Initializes the Weather SDK.
     *
     * @param apiKey                 the OpenWeather API key
     * @param mode                   the update mode (POLLING or ON_DEMAND)
     * @param pollingIntervalSeconds the polling interval in seconds (used only in POLLING mode)
     * @throws WeatherSdkException if the apiKey is null or empty, or if pollingIntervalSeconds <= 0 in POLLING mode
     */
    WeatherSdkImpl(String apiKey, UpdateMode mode, int pollingIntervalSeconds) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSdkException("API key cannot be null or empty.");
        }
        this.apiClient = new OpenWeatherApiClient(apiKey);
        this.cacheManager = new WeatherCacheManager();

        this.mode = mode;
        this.pollingIntervalSeconds = pollingIntervalSeconds;
        switch (mode) {
            case POLLING:
                if (pollingIntervalSeconds <= 0) {
                    throw new WeatherSdkException("Polling interval must be greater than 0 seconds.");
                }
                this.weatherUpdater = new WeatherUpdater(cacheManager, apiClient, pollingIntervalSeconds);
                log.info("Initialized with POLLING mode, interval={}s", pollingIntervalSeconds);
                break;
            case ON_DEMAND:
                if (pollingIntervalSeconds != 0) {
                    throw new WeatherSdkException("Polling interval must be 0 in ON_DEMAND mode.");
                }
                log.info("Initialized in ON_DEMAND mode (no polling)");
                break;
            default:
                throw new WeatherSdkException("Unsupported update mode.");
        }
    }

    @Override
    public String getWeather(String cityName) {
        checkShutdown();
        validateCityName(cityName);
        try {
            String cached = cacheManager.getCachedData(cityName);
            if (cached != null) {
                log.debug("Returning cached weather for '{}'", cityName);
                return cached;
            }
        } catch (CityNotFoundException e) {
            log.debug("City '{}' not found in cache, fetching from API...", cityName);
        }
        return fetchAndCache(cityName);
    }

    @Override
    public void updateWeather(String cityName) {
        checkShutdown();
        validateCityName(cityName);
        fetchAndCache(cityName);
    }

    @Override
    public List<String> getCachedCities() {
        checkShutdown();
        Set<String> set = cacheManager.getCachedCities();
        return new ArrayList<>(set);
    }

    @Override
    public void clearCache() {
        checkShutdown();
        log.info("Clearing cache...");
        cacheManager.clearCache();
    }

    @Override
    public boolean isPollingEnabled() {
        checkShutdown();
        return weatherUpdater != null;
    }

    @Override
    public void stopPolling() {
        checkShutdown();
        if (weatherUpdater != null) {
            weatherUpdater.stop();
            weatherUpdater = null;
            log.info("Polling stopped.");
        }
    }

    @Override
    public void shutdown() {
        if (!isShutdown) {
            stopPolling();
            clearCache();
            isShutdown = true;
            log.info("WeatherSdk instance has been shut down.");
        }
    }

    /**
     * Fetches and caches weather data for the given city.
     *
     * @param cityName the name of the city
     * @return the formatted weather data as JSON
     * @throws CityNotFoundException if the city is not found in the API response
     * @throws WeatherSdkException   if an error occurs while fetching data
     * @throws JsonParsingException  if the API response cannot be parsed
     */
    private String fetchAndCache(String cityName) {
        lock.lock();
        try {
            try {
                String cached = cacheManager.getCachedData(cityName);
                if (cached != null) {
                    log.debug("Found city '{}' in cache during fetchAndCache, returning it.", cityName);
                    return cached;
                }
            } catch (CityNotFoundException e) {
                log.debug("City '{}' not in cache, proceeding to fetch from API.", cityName);
            }
            String apiResponse = apiClient.fetchWeather(cityName);
            cacheManager.updateCache(cityName, apiResponse);
            log.info("Fetched and cached weather for '{}'.", cityName);
            return apiResponse;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Validates the provided city name.
     *
     * @param cityName the city name to validate
     * @throws InvalidCityException if the city name is null or empty
     */
    private void validateCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new InvalidCityException("City name cannot be null or empty.");
        }
    }

    private void checkShutdown() {
        if (isShutdown) {
            throw new SdkShutdownException("This WeatherSdk instance has been shut down and cannot be used.");
        }
    }
}