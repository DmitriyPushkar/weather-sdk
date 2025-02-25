package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
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

    public static WeatherSdk getInstance(String apiKey, int pollingIntervalSeconds) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty.");
        }
        lock.lock();
        try {
            if (instances.containsKey(apiKey)) {
                log.info("Returning existing WeatherSdk instance for API key: {}", apiKey);
            } else {
                log.info("Creating new WeatherSdk instance for API key: {}", apiKey);
            }
            return instances.computeIfAbsent(apiKey, key -> new WeatherSdkImpl(key, pollingIntervalSeconds));
        } finally {
            lock.unlock();
        }
    }

    public static void removeInstance(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty.");
        }
        lock.lock();
        try {
            if (instances.containsKey(apiKey)) {
                instances.remove(apiKey);
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
