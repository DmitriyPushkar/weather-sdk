package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemperatureData {
    @JsonProperty("temp")
    private double temp;

    @JsonProperty("feels_like")
    private double feelsLike;

    TemperatureData() {}
}

