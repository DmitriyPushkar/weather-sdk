package com.example.weathersdk.api;

import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.JsonParsingException;
import com.example.weathersdk.exception.WeatherSdkException;

import java.util.List;

/**
 * Interface for interacting with the Weather SDK.
 * Provides methods for retrieving and managing weather data.
 */
public interface WeatherSdk {

    /**
     * Retrieves the current weather for the specified city.
     * If cached data is available and still valid, returns the cached value.
     *
     * @param cityName the name of the city
     * @return a JSON string containing weather data
     * @throws InvalidCityException   if the city name is invalid or empty
     * @throws CityNotFoundException  if the city is not found in the API response
     * @throws WeatherSdkException    if an error occurs while fetching weather data
     * @throws JsonParsingException   if the API response cannot be parsed
     */
    String getWeather(String cityName);

    /**
     * Forces an update of the weather data for the specified city.
     * Fetches new data from the API and updates the cache.
     *
     * @param cityName the name of the city
     * @throws InvalidCityException   if the city name is invalid or empty
     * @throws CityNotFoundException  if the city is not found in the API response
     * @throws WeatherSdkException    if an error occurs while fetching weather data
     * @throws JsonParsingException   if the API response cannot be parsed
     */
    void updateWeather(String cityName);

    /**
     * Returns a list of city names currently stored in the cache.
     *
     * @return a list of cached city names
     */
    List<String> getCachedCities();

    /**
     * Clears all cached weather data.
     */
    void clearCache();

    /**
     * Checks if polling mode is currently enabled.
     *
     * @return {@code true} if polling is enabled, {@code false} otherwise
     */
    boolean isPollingEnabled();

    /**
     * Stops the background polling process if polling mode is enabled.
     */
    void stopPolling();
}