package com.example.weathersdk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// Client for interacting with the OpenWeather API
public class OpenWeatherApiClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final WebClient webClient;
    private final String apiKey;

    public OpenWeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public String fetchWeather(String cityName) {
        return null;
    }

    private Mono<Throwable> handleClientError(HttpStatusCode status, String errorBody) {
        return null;
    }
}
