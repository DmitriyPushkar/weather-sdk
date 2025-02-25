package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.enums.UpdateMode;
import com.example.weathersdk.exception.InvalidApiKeyException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class WeatherSdkFactory {

    private static final ConcurrentHashMap<String, WeatherSdk> instances = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    private WeatherSdkFactory() {
    }

    /**
     * Retrieves an existing {@link WeatherSdk} instance for the given apiKey,
     * or creates a new one if it does not exist or parameters have changed.
     *
     * @param apiKey                 the OpenWeather API key
     * @param mode                   the update mode (POLLING or ON_DEMAND)
     * @param pollingIntervalSeconds the polling interval in seconds (used only in POLLING mode);
     *                               warning: setting this value to less than 60 seconds may lead
     *                               to exceeding OpenWeatherMap's API rate limits and potential
     *                               account suspension.
     * @return an instance of {@link WeatherSdk}
     * @throws InvalidApiKeyException if the apiKey is null or empty
     */
    public static WeatherSdk getInstance(String apiKey, UpdateMode mode, int pollingIntervalSeconds) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty.");
        }
        lock.lock();
        try {
            if (instances.containsKey(apiKey)) {
                WeatherSdk existingInstance = instances.get(apiKey);
                if (needsUpdate(existingInstance, mode, pollingIntervalSeconds)) {
                    log.info("Parameters have changed, creating a new instance for API key: {}", apiKey);
                    removeInstance(apiKey);
                } else {
                    log.info("Returning existing WeatherSdk instance for API key: {}", apiKey);
                    return existingInstance;
                }
            }
            log.info("Creating new WeatherSdk instance for API key: {}", apiKey);
            return createNewInstance(apiKey, mode, pollingIntervalSeconds);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Determines if the current instance's parameters need to be updated based on new parameters.
     *
     * @param existingInstance the current instance
     * @param mode the new mode
     * @param pollingIntervalSeconds the new polling interval
     * @return true if the parameters have changed, false otherwise
     */
    private static boolean needsUpdate(WeatherSdk existingInstance, UpdateMode mode, int pollingIntervalSeconds) {
        WeatherSdkImpl sdkImpl = (WeatherSdkImpl) existingInstance;
        return sdkImpl.getMode() != mode || sdkImpl.getPollingIntervalSeconds() != pollingIntervalSeconds;
    }

    /**
     * Creates a new {@link WeatherSdk} instance.
     *
     * @param apiKey the OpenWeather API key
     * @param mode the update mode
     * @param pollingIntervalSeconds the polling interval in seconds
     * @return the newly created instance of {@link WeatherSdk}
     */
    private static WeatherSdk createNewInstance(String apiKey, UpdateMode mode, int pollingIntervalSeconds) {
        WeatherSdk newInstance = new WeatherSdkImpl(apiKey, mode, pollingIntervalSeconds);
        instances.put(apiKey, newInstance);
        return newInstance;
    }

    /**
     * Removes an existing {@link WeatherSdk} instance associated with the given API key.
     *
     * @param apiKey the OpenWeather API key
     * @throws InvalidApiKeyException if the API key is null, empty, or does not have an associated instance
     */
    public static void removeInstance(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty.");
        }
        lock.lock();
        try {
            WeatherSdk instance = instances.remove(apiKey);
            if (instance != null) {
                instance.shutdown();
                log.info("Removed WeatherSdk instance for API key: {}", apiKey);
            } else {
                log.warn("Attempted to remove non-existent WeatherSdk instance for API key: {}", apiKey);
                throw new InvalidApiKeyException("No instance found for the given API key.");
            }
        } finally {
            lock.unlock();
        }
    }
}