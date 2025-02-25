package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing weather condition data from the OpenWeather API.
 * Contains general weather information and a detailed description.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    /**
     * Main weather condition (e.g., "Clear", "Clouds", "Rain").
     */
    @JsonProperty("main")
    private String main;

    /**
     * Detailed weather description (e.g., "scattered clouds", "light rain").
     */
    @JsonProperty("description")
    private String description;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    WeatherData() {}
}
