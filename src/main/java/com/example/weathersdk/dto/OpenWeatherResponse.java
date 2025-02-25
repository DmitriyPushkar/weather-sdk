package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing the response from the OpenWeather API.
 * This class is used for deserialization of JSON responses from the API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {

    /**
     * Array of weather conditions.
     */
    @JsonProperty("weather")
    private WeatherData[] weather;

    /**
     * Temperature-related data, including actual and perceived temperature.
     */
    @JsonProperty("main")
    private TemperatureData main;

    /**
     * Visibility in meters.
     */
    @JsonProperty("visibility")
    private int visibility;

    /**
     * Wind-related data, including speed.
     */
    @JsonProperty("wind")
    private WindData wind;

    /**
     * Unix timestamp of the weather data.
     */
    @JsonProperty("dt")
    private long datetime;

    /**
     * System-related data, including sunrise and sunset times.
     */
    @JsonProperty("sys")
    private SystemData sys;

    /**
     * Timezone offset in seconds from UTC.
     */
    @JsonProperty("timezone")
    private int timezone;

    /**
     * Name of the location (city name).
     */
    @JsonProperty("name")
    private String name;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    OpenWeatherResponse() {}
}
