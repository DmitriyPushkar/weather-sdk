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
            sdkJson.set("weather", parseWeather(root));
            sdkJson.set("temperature", parseTemperature(root));
            sdkJson.put("visibility", root.path("visibility").asInt(0));
            sdkJson.set("wind", parseWind(root));
            sdkJson.put("datetime", root.path("dt").asLong(0));
            sdkJson.set("sys", parseSys(root));
            sdkJson.put("timezone", root.path("timezone").asLong(0));
            sdkJson.put("name", root.path("name").asText("Unknown"));

            return objectMapper.writeValueAsString(sdkJson);

        } catch (IOException e) {
            log.error("Failed to parse JSON from OpenWeather API", e);
            throw new JsonParsingException("Failed to parse JSON from OpenWeather", e);
        }
    }

    private ObjectNode parseWeather(JsonNode root) {
        ObjectNode weatherNode = objectMapper.createObjectNode();
        JsonNode weatherArray = root.path("weather");

        if (weatherArray.isArray() && !weatherArray.isEmpty()) {
            JsonNode firstWeather = weatherArray.get(0);
            weatherNode.put("main",        firstWeather.path("main").asText(""));
            weatherNode.put("description", firstWeather.path("description").asText(""));
        }
        return weatherNode;
    }

    private ObjectNode parseTemperature(JsonNode root) {
        ObjectNode temperatureNode = objectMapper.createObjectNode();
        JsonNode mainNode = root.path("main");

        temperatureNode.put("temp",       mainNode.path("temp").asDouble(0.0));
        temperatureNode.put("feels_like", mainNode.path("feels_like").asDouble(0.0));
        return temperatureNode;
    }

    private ObjectNode parseWind(JsonNode root) {
        ObjectNode windNode = objectMapper.createObjectNode();
        JsonNode windRoot = root.path("wind");

        windNode.put("speed", windRoot.path("speed").asDouble(0.0));
        return windNode;
    }

    private ObjectNode parseSys(JsonNode root) {
        ObjectNode sysNode = objectMapper.createObjectNode();
        JsonNode sysRoot = root.path("sys");

        sysNode.put("sunrise", sysRoot.path("sunrise").asLong(0));
        sysNode.put("sunset",  sysRoot.path("sunset").asLong(0));
        return sysNode;
    }
}