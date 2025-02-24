package com.example.weathersdk.service;

import com.example.weathersdk.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OpenWeatherApiClientTest {
    private MockWebServer mockWebServer;
    private OpenWeatherApiClient apiClient;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        apiClient = new OpenWeatherApiClient("test-api-key", baseUrl);
    }


    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchWeather_ReturnsJsonResponse() {
        String mockResponse = """
                   {
                      "coord": {
                        "lon": 10.9904,
                        "lat": 44.3473
                      },
                      "weather": [
                        {
                          "id": 804,
                          "main": "Clouds",
                          "description": "overcast clouds",
                          "icon": "04d"
                        }
                      ],
                      "base": "stations",
                      "main": {
                        "temp": 48.78,
                        "feels_like": 48.78,
                        "temp_min": 47.7,
                        "temp_max": 48.79,
                        "pressure": 1026,
                        "humidity": 95,
                        "sea_level": 1026,
                        "grnd_level": 963
                      },
                      "visibility": 10000,
                      "wind": {
                        "speed": 2.46,
                        "deg": 145,
                        "gust": 4.43
                      },
                      "clouds": {
                        "all": 100
                      },
                      "dt": 1740406884,
                      "sys": {
                        "type": 2,
                        "id": 2004688,
                        "country": "IT",
                        "sunrise": 1740376916,
                        "sunset": 1740416215
                      },
                      "timezone": 3600,
                      "id": 3163858,
                      "name": "Zocca",
                      "cod": 200
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String jsonResponse = apiClient.fetchWeather("Zocca");

        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.trim().startsWith("{"));
        assertTrue(jsonResponse.contains("\"main\":\"Clouds\""));
        assertTrue(jsonResponse.contains("\"description\":\"overcast clouds\""));
        assertTrue(jsonResponse.contains("\"temp\":48.78"));
        assertTrue(jsonResponse.contains("\"feels_like\":48.78"));
        assertTrue(jsonResponse.contains("\"visibility\":10000"));
        assertTrue(jsonResponse.contains("\"speed\":2.46"));
        assertTrue(jsonResponse.contains("\"datetime\":1740406884"));
        assertTrue(jsonResponse.contains("\"sunrise\":1740376916"));
        assertTrue(jsonResponse.contains("\"sunset\":1740416215"));
        assertTrue(jsonResponse.contains("\"timezone\":3600"));
        assertTrue(jsonResponse.contains("\"name\":\"Zocca\""));
    }

    @Test
    void openWeatherApiClient_ThrowsException_WhenApiKeyIsNull() {
        assertThrows(InvalidApiKeyException.class, () -> new OpenWeatherApiClient(null));
    }

    @Test
    void openWeatherApiClient_ThrowsException_WhenApiKeyIsEmpty() {
        assertThrows(InvalidApiKeyException.class, () -> new OpenWeatherApiClient(""));
    }

    @Test
    void openWeatherApiClient_ThrowsException_WhenApiKeyIsBlank() {
        assertThrows(InvalidApiKeyException.class, () -> new OpenWeatherApiClient("  "));
    }

    @Test
    void fetchWeather_ThrowsInvalidCityException_WhenCityNameIsNull() {
        assertThrows(InvalidCityException.class, () -> apiClient.fetchWeather(null));
    }

    @Test
    void fetchWeather_ThrowsInvalidCityException_WhenCityNameIsEmpty() {
        assertThrows(InvalidCityException.class, () -> apiClient.fetchWeather(""));
    }

    @Test
    void fetchWeather_ThrowsNetworkException_WhenTimeoutOccurs() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"message\":\"success\"}")
                .setBodyDelay(5, TimeUnit.SECONDS)
        );

        assertThrows(NetworkException.class, () -> apiClient.fetchWeather("London"));
    }

    @Test
    void fetchWeather_ThrowsWeatherSdkException_WhenBadRequest() {
        String errorResponse = """
                    {
                      "cod": "400",
                      "message": "Invalid request"
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));

        assertThrows(WeatherSdkException.class, () -> apiClient.fetchWeather("!@#$$%"));
    }

    @Test
    void fetchWeather_ThrowsInvalidApiKeyException() {
        String errorResponse = """
                    {
                      "cod": "401",
                      "message": "Invalid API key"
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));

        assertThrows(InvalidApiKeyException.class, () -> apiClient.fetchWeather("London"));
    }

    @Test
    void fetchWeather_ThrowsInvalidApiKeyException_WhenApiKeyIsBlocked() {
        String errorResponse = """
                    {
                      "cod": "403",
                      "message": "Your API key has been blocked"
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));

        assertThrows(ApiKeyBlockedException.class, () -> apiClient.fetchWeather("London"));
    }

    @Test
    void fetchWeather_ThrowsCityNotFoundException() {
        String errorResponse = """
                    {
                      "cod": "404",
                      "message": "city not found"
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));

        assertThrows(CityNotFoundException.class, () -> apiClient.fetchWeather("NonExistentCity"));
    }

    @Test
    void fetchWeather_ThrowsUnexpectedApiException_WhenServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"cod\": \"500\", \"message\": \"Internal Server Error\"}")
                .addHeader("Content-Type", "application/json"));

        assertThrows(UnexpectedApiException.class, () -> apiClient.fetchWeather("London"));
    }
}
