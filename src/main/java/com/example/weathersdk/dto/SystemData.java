package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemData {
    @JsonProperty("sunrise")
    private long sunrise;

    @JsonProperty("sunset")
    private long sunset;

    SystemData() {}
}