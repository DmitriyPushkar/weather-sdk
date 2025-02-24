package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    @JsonProperty("weather")
    private WeatherData[] weather;

    @JsonProperty("main")
    private TemperatureData main;

    @JsonProperty("visibility")
    private int visibility;

    @JsonProperty("wind")
    private WindData wind;

    @JsonProperty("dt")
    private long datetime;

    @JsonProperty("sys")
    private SystemData sys;

    @JsonProperty("timezone")
    private int timezone;

    @JsonProperty("name")
    private String name;

    // Закрываем возможность создавать объект DTO вручную за пределами пакета
    OpenWeatherResponse() {}
}
