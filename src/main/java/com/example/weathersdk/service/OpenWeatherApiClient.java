package com.example.weathersdk.service;

import com.example.weathersdk.dto.OpenWeatherResponse;
import com.example.weathersdk.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
class OpenWeatherApiClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final String apiKey;

    public OpenWeatherApiClient(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public OpenWeatherApiClient(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidApiKeyException("API key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String fetchWeather(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new InvalidCityException("City name cannot be null or empty");
        }
        log.debug("Fetching weather for city='{}' with apiKey='{}'", cityName, apiKey);
        try {
            String result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", cityName)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "imperial")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody ->
                                            handleErrorResponse(response.statusCode(), errorBody))
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody ->
                                            Mono.error(new UnexpectedApiException(
                                                    "OpenWeather API server error: " + errorBody, null)))
                    )
                    .bodyToMono(OpenWeatherResponse.class)
                    .map(this::convertToRequiredFormat)
                    .onErrorMap(ex -> !(ex instanceof WeatherSdkException),
                            ex -> new NetworkException("Network error: " + ex.getMessage(), ex))
                    .block();

            log.debug("Successfully fetched weather for city='{}'", cityName);
            return result;
        } catch (WeatherSdkException e) {
            log.warn("WeatherSdkException occurred for city='{}': {}", cityName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected network error for city='{}': {}", cityName, e.getMessage(), e);
            throw new NetworkException("Unexpected network error: " + e.getMessage(), e);
        }
    }

    private String convertToRequiredFormat(OpenWeatherResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(Map.of(
                    "weather", Map.of(
                            "main", response.getWeather()[0].getMain(),
                            "description", response.getWeather()[0].getDescription()
                    ),
                    "temperature", Map.of(
                            "temp", response.getMain().getTemp(),
                            "feels_like", response.getMain().getFeelsLike()
                    ),
                    "visibility", response.getVisibility(),
                    "wind", Map.of(
                            "speed", response.getWind().getSpeed()
                    ),
                    "datetime", response.getDatetime(),
                    "sys", Map.of(
                            "sunrise", response.getSys().getSunrise(),
                            "sunset", response.getSys().getSunset()
                    ),
                    "timezone", response.getTimezone(),
                    "name", response.getName()
            ));
        } catch (Exception e) {
            log.error("Error while converting response to JSON: {}", e.getMessage(), e);
            throw new WeatherSdkException("Error while processing weather data", e);
        }
    }

    private Mono<Throwable> handleErrorResponse(HttpStatusCode status, String errorBody) {
        log.warn("Client error from OpenWeather API (status={}): {}", status.value(), errorBody);
        String errorMessage = extractErrorMessage(errorBody);
        return switch (status.value()) {
            case 401 -> Mono.error(new InvalidApiKeyException("Invalid API Key: " + errorMessage));
            case 403 -> Mono.error(new ApiKeyBlockedException("API Key blocked: " + errorMessage));
            case 404 -> Mono.error(new CityNotFoundException("City not found: " + errorMessage));
            default -> Mono.error(new WeatherSdkException("API error: " + errorMessage));
        };
    }

    private String extractErrorMessage(String errorBody) {
        try {
            Map<String, Object> errorMap = OBJECT_MAPPER.readValue(errorBody, Map.class);
            return (String) errorMap.getOrDefault("message", "Unknown error");
        } catch (Exception e) {
            log.error("Failed to parse error response: {}", e.getMessage(), e);
            return "Unknown error (failed to parse response)";
        }
    }
}
