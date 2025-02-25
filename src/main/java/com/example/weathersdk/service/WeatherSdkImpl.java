package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.enums.UpdateMode;
import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.JsonParsingException;
import com.example.weathersdk.exception.WeatherSdkException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
    private final ObjectMapper objectMapper;
    private final ReentrantLock lock = new ReentrantLock();
    private WeatherUpdater weatherUpdater;

    @Getter
    private final UpdateMode mode;
    @Getter
    private final int pollingIntervalSeconds;

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
        this.objectMapper = new ObjectMapper();

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
    public List<String> getCachedCities() {
        Set<String> set = cacheManager.getCachedCities();
        return new ArrayList<>(set);
    }

    @Override
    public void clearCache() {
        log.info("Clearing cache...");
        cacheManager.clearCache();
    }

    @Override
    public boolean isPollingEnabled() {
        return weatherUpdater != null;
    }

    @Override
    public void stopPolling() {
        if (weatherUpdater != null) {
            weatherUpdater.stop();
            weatherUpdater = null;
            log.info("Polling stopped.");
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
            String formattedJson = formatWeatherResponse(apiResponse);
            cacheManager.updateCache(cityName, formattedJson);
            log.info("Fetched and cached weather for '{}'.", cityName);
            return formattedJson;
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

    /**
     * Formats the raw JSON response from the OpenWeather API into the expected structure.
     *
     * @param rawJson the raw API response
     * @return the formatted JSON string
     * @throws JsonParsingException if the response cannot be parsed
     */
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

    /**
     * Extracts weather information from the API response.
     *
     * @param root the root JSON node
     * @return an {@link ObjectNode} containing weather data
     */
    private ObjectNode parseWeather(JsonNode root) {
        ObjectNode weatherNode = objectMapper.createObjectNode();
        JsonNode weatherArray = root.path("weather");

        if (weatherArray.isArray() && !weatherArray.isEmpty()) {
            JsonNode firstWeather = weatherArray.get(0);
            weatherNode.put("main", firstWeather.path("main").asText(""));
            weatherNode.put("description", firstWeather.path("description").asText(""));
        }
        return weatherNode;
    }

    /**
     * Extracts temperature information from the API response.
     *
     * @param root the root JSON node
     * @return an {@link ObjectNode} containing temperature data
     */
    private ObjectNode parseTemperature(JsonNode root) {
        ObjectNode temperatureNode = objectMapper.createObjectNode();
        JsonNode mainNode = root.path("main");

        temperatureNode.put("temp", mainNode.path("temp").asDouble(0.0));
        temperatureNode.put("feels_like", mainNode.path("feels_like").asDouble(0.0));
        return temperatureNode;
    }

    /**
     * Extracts wind information from the API response.
     *
     * @param root the root JSON node
     * @return an {@link ObjectNode} containing wind data
     */
    private ObjectNode parseWind(JsonNode root) {
        ObjectNode windNode = objectMapper.createObjectNode();
        JsonNode windRoot = root.path("wind");

        windNode.put("speed", windRoot.path("speed").asDouble(0.0));
        return windNode;
    }

    /**
     * Extracts system information (sunrise, sunset) from the API response.
     *
     * @param root the root JSON node
     * @return an {@link ObjectNode} containing system data
     */
    private ObjectNode parseSys(JsonNode root) {
        ObjectNode sysNode = objectMapper.createObjectNode();
        JsonNode sysRoot = root.path("sys");

        sysNode.put("sunrise", sysRoot.path("sunrise").asLong(0));
        sysNode.put("sunset", sysRoot.path("sunset").asLong(0));
        return sysNode;
    }
}