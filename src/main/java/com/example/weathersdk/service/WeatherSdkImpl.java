package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.JsonParsingException;
import com.example.weathersdk.exception.WeatherSdkException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class WeatherSdkImpl implements WeatherSdk {
    private final OpenWeatherApiClient apiClient;
    private final WeatherCacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final ReentrantLock lock = new ReentrantLock();
    private WeatherUpdater weatherUpdater;

    WeatherSdkImpl(String apiKey, int pollingIntervalSeconds) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSdkException("API key cannot be null or empty.");
        }
        this.apiClient = new OpenWeatherApiClient(apiKey);
        this.cacheManager = new WeatherCacheManager();
        this.objectMapper = new ObjectMapper();

        if (pollingIntervalSeconds > 0) {
            this.weatherUpdater = new WeatherUpdater(cacheManager, apiClient, pollingIntervalSeconds);
            log.info("Initialized with POLLING mode, interval={}s", pollingIntervalSeconds);
        } else {
            log.info("Initialized in ON_DEMAND mode (no polling)");
        }
    }

    @Override
    public String getWeather(String cityName) {
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
        validateCityName(cityName);
        fetchAndCache(cityName);
    }

    @Override
    public void stopPolling() {
        if (weatherUpdater != null) {
            weatherUpdater.stop();
            weatherUpdater = null;
            log.info("Polling stopped.");
        }
    }

    @Override
    public boolean isPollingEnabled() {
        return weatherUpdater != null;
    }

    @Override
    public List<String> getCachedCities() {
        Set<String> set = cacheManager.getCachedCities();
        return new ArrayList<>(set);
    }

    @Override
    public void clearCache() {
        log.info("Clearing cache...");
        cacheManager.clearCache();
    }

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
            String formattedJson = formatWeatherResponse(apiResponse);
            cacheManager.updateCache(cityName, formattedJson);
            log.info("Fetched and cached weather for '{}'.", cityName);
            return formattedJson;
        } finally {
            lock.unlock();
        }
    }

    private void validateCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new InvalidCityException("City name cannot be null or empty.");
        }
    }

    private String formatWeatherResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            ObjectNode sdkJson = objectMapper.createObjectNode();
            ObjectNode weatherNode = objectMapper.createObjectNode();
            JsonNode weatherArray = root.get("weather");
            if (weatherArray != null && weatherArray.isArray() && !weatherArray.isEmpty()) {
                weatherNode.put("main", weatherArray.get(0).get("main").asText(""));
                weatherNode.put("description", weatherArray.get(0).get("description").asText(""));
            }
            sdkJson.set("weather", weatherNode);

            ObjectNode tempNode = objectMapper.createObjectNode();
            JsonNode mainNode = root.get("main");
            if (mainNode != null) {
                tempNode.put("temp", mainNode.get("temp").asDouble(0));
                tempNode.put("feels_like", mainNode.get("feels_like").asDouble(0));
            }
            sdkJson.set("temperature", tempNode);

            sdkJson.put("visibility", root.get("visibility") == null ? 0 : root.get("visibility").asInt(0));

            ObjectNode windNode = objectMapper.createObjectNode();
            JsonNode windRoot = root.get("wind");
            if (windRoot != null) {
                windNode.put("speed", windRoot.get("speed").asDouble(0));
            }
            sdkJson.set("wind", windNode);

            sdkJson.put("datetime", root.get("dt") == null ? 0 : root.get("dt").asLong(0));

            ObjectNode sysNode = objectMapper.createObjectNode();
            JsonNode sysRoot = root.get("sys");
            if (sysRoot != null) {
                sysNode.put("sunrise", sysRoot.get("sunrise") == null ? 0 : sysRoot.get("sunrise").asLong(0));
                sysNode.put("sunset", sysRoot.get("sunset") == null ? 0 : sysRoot.get("sunset").asLong(0));
            }
            sdkJson.set("sys", sysNode);

            sdkJson.put("timezone", root.get("timezone") == null ? 0 : root.get("timezone").asLong(0));
            sdkJson.put("name", root.get("name") == null ? "Unknown" : root.get("name").asText("Unknown"));
            return objectMapper.writeValueAsString(sdkJson);
        } catch (IOException e) {
            log.error("Failed to parse JSON from OpenWeather API", e);
            throw new JsonParsingException("Failed to parse JSON from OpenWeather", e);
        }
    }
}