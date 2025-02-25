package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO representing wind-related weather data from the OpenWeather API.
 * Contains information about wind speed.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WindData {

    /**
     * Wind speed in meters per second.
     */
    @Min(value = 0, message = "Wind speed cannot be negative")
    @JsonProperty("speed")
    private double speed;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    WindData() {}
}