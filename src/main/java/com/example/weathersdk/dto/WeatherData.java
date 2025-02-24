package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {
    @JsonProperty("main")
    private String main;

    @JsonProperty("description")
    private String description;

    WeatherData() {}
}

