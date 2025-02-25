package com.example.weathersdk.service;

import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.WeatherSdkException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages caching of weather data for a limited number of cities.
 * Uses Caffeine cache with an expiration policy to store weather data efficiently.
 */
@Slf4j
class WeatherCacheManager {

    private static final int MAX_CITIES = 10;
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(10);

    private final Cache<String, String> cache;
    private final ConcurrentLinkedQueue<String> cityOrder = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, Boolean> citySet = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a WeatherCacheManager with a system ticker.
     */
    WeatherCacheManager() {
        this(Ticker.systemTicker());
    }

    /**
     * Constructs a WeatherCacheManager with a custom ticker.
     * This constructor is primarily used for testing purposes.
     *
     * @param ticker the ticker used for cache expiration timing
     */
    WeatherCacheManager(Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(MAX_CITIES)
                .expireAfterWrite(EXPIRATION_TIME)
                .ticker(ticker)
                .build();
    }

    /**
     * Retrieves cached weather data for the specified city.
     *
     * @param cityName the name of the city
     * @return the cached weather data as a JSON string
     * @throws InvalidCityException  if the city name is null or empty
     * @throws CityNotFoundException if the weather data is not found in the cache
     */
    public String getCachedData(final String cityName) {
        validateCityName(cityName);

        String data = cache.getIfPresent(cityName);
        if (data == null) {
            log.warn("Weather data for city '{}' is not found in cache.", cityName);
            throw new CityNotFoundException("Weather data for city '" + cityName + "' is not found in cache.");
        }
        log.debug("Returning cached weather data for city '{}'", cityName);
        return data;
    }

    /**
     * Updates or adds weather data to the cache for the given city.
     * If the cache exceeds the maximum size, the oldest entry is removed.
     *
     * @param cityName the name of the city
     * @param data     the weather data as a JSON string
     * @throws InvalidCityException if the city name is null or empty
     * @throws WeatherSdkException  if the weather data is null or empty
     */
    public void updateCache(final String cityName, final String data) {
        validateCityName(cityName);
        if (data == null || data.trim().isEmpty()) {
            throw new WeatherSdkException("Weather data cannot be null or empty for city: " + cityName);
        }

        boolean alreadyExists = citySet.putIfAbsent(cityName, true) != null;
        cache.put(cityName, data);

        synchronized (this) {
            if (!alreadyExists) {
                cityOrder.add(cityName);
                log.info("Added new city '{}' to cache", cityName);
            } else {
                log.debug("Updated weather data for city '{}'", cityName);
            }

            if (cityOrder.size() > MAX_CITIES) {
                removeOldestEntry();
            }
        }
    }

    /**
     * Retrieves the set of city names currently stored in the cache.
     *
     * @return a set of cached city names
     */
    public Set<String> getCachedCities() {
        return cache.asMap().keySet();
    }

    /**
     * Clears all cached weather data.
     */
    public void clearCache() {
        lock.lock();
        try {
            cache.invalidateAll();
            cityOrder.clear();
            citySet.clear();
            log.info("Cache cleared.");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the oldest cached city if the cache exceeds its size limit.
     */
    private void removeOldestEntry() {
        lock.lock();
        try {
            while (cityOrder.size() > MAX_CITIES) {
                String oldestCity = cityOrder.poll();
                if (oldestCity != null) {
                    cache.invalidate(oldestCity);
                    citySet.remove(oldestCity);
                    log.debug("Evicted oldest city '{}' from cache", oldestCity);
                }
            }
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
}