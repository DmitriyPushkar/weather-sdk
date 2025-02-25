package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Valid
    @NotNull(message = "Weather data cannot be null")
    @Size(min = 1, message = "At least one weather condition must be provided")
    @JsonProperty("weather")
    private WeatherData[] weather;

    /**
     * Temperature-related data, including actual and perceived temperature.
     */
    @Valid
    @NotNull(message = "Temperature data cannot be null")
    @JsonProperty("main")
    private TemperatureData main;

    /**
     * Visibility in meters.
     */
    @Min(value = 1, message = "Visibility cannot be zero or negative")
    @JsonProperty("visibility")
    private int visibility;

    /**
     * Wind-related data, including speed.
     */
    @Valid
    @NotNull(message = "Wind data cannot be null")
    @JsonProperty("wind")
    private WindData wind;

    /**
     * Unix timestamp of the weather data.
     */
    @Min(value = 1, message = "DateTime cannot be zero or negative")
    @JsonProperty("dt")
    private long datetime;

    /**
     * System-related data, including sunrise and sunset times.
     */
    @Valid
    @NotNull(message = "System data cannot be null")
    @JsonProperty("sys")
    private SystemData sys;

    /**
     * Timezone offset in seconds from UTC.
     */
    @Min(value = 1, message = "Timezone cannot be zero or negative")
    @JsonProperty("timezone")
    private int timezone;

    /**
     * Name of the location (city name).
     */
    @NotBlank(message = "City name cannot be empty")
    @JsonProperty("name")
    private String name;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    OpenWeatherResponse() {}
}
