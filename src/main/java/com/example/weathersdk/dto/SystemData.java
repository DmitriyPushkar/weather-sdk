package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("sunrise")
    private long sunrise;

    /**
     * Unix timestamp of the sunset time.
     */
    @JsonProperty("sunset")
    private long sunset;

    /**
     * Private constructor to restrict manual instantiation outside the package.
     */
    SystemData() {}
}
