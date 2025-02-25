package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO representing temperature-related weather data from the OpenWeather API.
 * Contains information about the actual and perceived temperature.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemperatureData {

    /**
     * Current temperature in Kelvin.
     */
    @Min(value = 0, message = "Temperature must be a non-negative value (in Kelvin)")
    @JsonProperty("temp")
    private double temp;

    /**
     * Feels-like temperature in Kelvin.
     */
    @Min(value = 0, message = "Feels-like temperature must be a non-negative value (in Kelvin)")
    @JsonProperty("feels_like")
    private double feelsLike;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    TemperatureData() {}
}

