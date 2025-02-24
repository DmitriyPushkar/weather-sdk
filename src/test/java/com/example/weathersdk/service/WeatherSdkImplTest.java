package com.example.weathersdk.service;

import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.JsonParsingException;
import com.example.weathersdk.exception.WeatherSdkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WeatherSdkImplTest {
    private WeatherSdkImpl sdk;
    private OpenWeatherApiClient apiClientMock;
    private WeatherCacheManager cacheManagerMock;

    @BeforeEach
    void setUp() {
        apiClientMock = mock(OpenWeatherApiClient.class);
        cacheManagerMock = mock(WeatherCacheManager.class);

        sdk = new WeatherSdkImpl("validKey", 0);

        ReflectionTestUtils.setField(sdk, "apiClient", apiClientMock);
        ReflectionTestUtils.setField(sdk, "cacheManager", cacheManagerMock);
    }

    @Test
    void testConstructorThrowsExceptionForInvalidApiKey() {
        assertThrows(WeatherSdkException.class, () -> new WeatherSdkImpl("", 0));
        assertThrows(WeatherSdkException.class, () -> new WeatherSdkImpl("   ", 10));
    }

    @Test
    void testGetWeatherReturnsCachedData() {
        when(cacheManagerMock.getCachedData("London")).thenReturn("cached-london");
        String result = sdk.getWeather("London");
        assertEquals("cached-london", result);
        verify(cacheManagerMock).getCachedData(eq("London"));
        verifyNoMoreInteractions(cacheManagerMock);
    }

    @Test
    void testGetWeatherFetchesFromApiIfNotCached() {
        when(cacheManagerMock.getCachedData("Zocca")).thenReturn(null);
        when(apiClientMock.fetchWeather("Zocca")).thenReturn("{\n" +
                "  \"weather\": {\n" +
                "    \"main\": \"Clouds\",\n" +
                "    \"description\": \"overcast clouds\"\n" +
                "  },\n" +
                "  \"temperature\": {\n" +
                "    \"temp\": 48.78,\n" +
                "    \"feels_like\": 48.78\n" +
                "  },\n" +
                "  \"visibility\": 10000,\n" +
                "  \"wind\": {\n" +
                "    \"speed\": 2.46\n" +
                "  },\n" +
                "  \"datetime\": 1740406884,\n" +
                "  \"sys\": {\n" +
                "    \"sunrise\": 1740376916,\n" +
                "    \"sunset\": 1740416215\n" +
                "  },\n" +
                "  \"timezone\": 3600,\n" +
                "  \"name\": \"Zocca\"\n" +
                "}\n");

        String result = sdk.getWeather("Zocca");
        assertNotNull(result);
        verify(cacheManagerMock, times(2)).getCachedData("Zocca");
        verify(apiClientMock).fetchWeather("Zocca");
        verify(cacheManagerMock).updateCache(eq("Zocca"), anyString());
    }

    @Test
    void testGetWeatherThrowsInvalidCityException() {
        assertThrows(InvalidCityException.class, () -> sdk.getWeather(null));
        assertThrows(InvalidCityException.class, () -> sdk.getWeather("  "));
    }

    @Test
    void testStopPollingWithInterval() {
        WeatherSdkImpl pollingSdk = new WeatherSdkImpl("validKey", 5);
        assertTrue(pollingSdk.isPollingEnabled());
        pollingSdk.stopPolling();
        assertFalse(pollingSdk.isPollingEnabled());
    }

    @Test
    void testStopPollingWithZeroInterval() {
        assertFalse(sdk.isPollingEnabled());
        sdk.stopPolling();
        assertFalse(sdk.isPollingEnabled());
    }

    @Test
    void testClearCache() {
        assertDoesNotThrow(() -> sdk.clearCache());
        verify(cacheManagerMock).clearCache();
    }

    @Test
    void testUpdateWeather() {
        when(apiClientMock.fetchWeather("Zocca"))
                .thenReturn("{\n" +
                        "  \"weather\": {\n" +
                        "    \"main\": \"Clouds\",\n" +
                        "    \"description\": \"overcast clouds\"\n" +
                        "  },\n" +
                        "  \"temperature\": {\n" +
                        "    \"temp\": 48.78,\n" +
                        "    \"feels_like\": 48.78\n" +
                        "  },\n" +
                        "  \"visibility\": 10000,\n" +
                        "  \"wind\": {\n" +
                        "    \"speed\": 2.46\n" +
                        "  },\n" +
                        "  \"datetime\": 1740406884,\n" +
                        "  \"sys\": {\n" +
                        "    \"sunrise\": 1740376916,\n" +
                        "    \"sunset\": 1740416215\n" +
                        "  },\n" +
                        "  \"timezone\": 3600,\n" +
                        "  \"name\": \"Zocca\"\n" +
                        "}\n");
        when(cacheManagerMock.getCachedData("Moscow")).thenReturn(null);

        assertDoesNotThrow(() -> sdk.updateWeather("Zocca"));
        verify(cacheManagerMock).getCachedData("Zocca");
        verify(apiClientMock).fetchWeather("Zocca");
        verify(cacheManagerMock).updateCache(eq("Zocca"), anyString());
    }

    @Test
    void testGetWeatherWhenApiClientThrows() {
        when(cacheManagerMock.getCachedData("ExceptionCity")).thenReturn(null);
        when(apiClientMock.fetchWeather("ExceptionCity")).thenThrow(new WeatherSdkException("Network error"));

        assertThrows(WeatherSdkException.class, () -> sdk.getWeather("ExceptionCity"));

        verify(cacheManagerMock, times(2)).getCachedData("ExceptionCity");
        verify(apiClientMock).fetchWeather("ExceptionCity");
        verify(cacheManagerMock, never()).updateCache(anyString(), anyString());
    }

    @Test
    void testGetWeatherThrowsJsonParsingExceptionForBadJson() {
        when(cacheManagerMock.getCachedData("BadJsonCity")).thenReturn(null);
        when(apiClientMock.fetchWeather("BadJsonCity")).thenReturn("{{ this is not valid json");

        assertThrows(JsonParsingException.class, () -> sdk.getWeather("BadJsonCity"));

        verify(cacheManagerMock, times(2)).getCachedData("BadJsonCity");
        verify(apiClientMock).fetchWeather("BadJsonCity");
        verify(cacheManagerMock, never()).updateCache(anyString(), anyString());
    }

    @Test
    void testGetWeatherFetchesFromApiIfDataIsStale() {
        when(cacheManagerMock.getCachedData("StaleCity"))
                .thenThrow(new CityNotFoundException("Data is stale or city not found"));
        when(apiClientMock.fetchWeather("StaleCity")).thenReturn("{ \"weather\": [{\"main\": \"Clouds\", \"description\": \"...\"}], \"name\": \"StaleCity\" }");

        String result = sdk.getWeather("StaleCity");
        assertNotNull(result);

        verify(cacheManagerMock, times(2)).getCachedData("StaleCity");
        verify(apiClientMock).fetchWeather("StaleCity");
        verify(cacheManagerMock).updateCache(eq("StaleCity"), anyString());
    }

    @Test
    void testCacheLimitExceeded() {
        when(cacheManagerMock.getCachedData("City11")).thenReturn(null);
        doThrow(new WeatherSdkException("Cache limit exceeded"))
                .when(cacheManagerMock).updateCache(eq("City11"), anyString());

        when(apiClientMock.fetchWeather("City11"))
                .thenReturn("{ \"weather\":[{\"main\":\"Clouds\", \"description\":\"...\"}], \"name\":\"City11\" }");

        assertThrows(WeatherSdkException.class, () -> sdk.getWeather("City11"));

        verify(cacheManagerMock, times(2)).getCachedData("City11");
        verify(apiClientMock).fetchWeather("City11");
        verify(cacheManagerMock).updateCache(eq("City11"), anyString());
    }
}