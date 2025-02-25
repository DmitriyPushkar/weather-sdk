package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO representing system-related weather data from the OpenWeather API.
 * Contains information about sunrise and sunset times.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemData {

    /**
     * Unix timestamp of the sunrise time.
     */
    @Min(value = 1, message = "Sunrise time cannot be zero or negative")
    @JsonProperty("sunrise")
    private long sunrise;

    /**
     * Unix timestamp of the sunset time.
     */
    @Min(value = 1, message = "Sunset time cannot be zero or negative")
    @JsonProperty("sunset")
    private long sunset;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    SystemData() {}
}
