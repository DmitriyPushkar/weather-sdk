package com.example.weathersdk.service;

import com.example.weathersdk.exception.WeatherSdkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherUpdaterTest {
    private WeatherCacheManager cacheManager;
    private OpenWeatherApiClient apiClient;
    private WeatherUpdater updater;

    @BeforeEach
    void setUp() {
        cacheManager = mock(WeatherCacheManager.class);
        apiClient = mock(OpenWeatherApiClient.class);
    }

    @Test
    void testUpdaterStartsAndStopsCorrectly() {
        updater = new WeatherUpdater(cacheManager, apiClient, 2);
        assertFalse(isSchedulerShutdown(updater));

        updater.stop();
        assertTrue(isSchedulerShutdown(updater));
    }

    @Test
    void testWeatherUpdaterUpdatesCache() {
        when(cacheManager.getCachedCities()).thenReturn(Set.of("London", "Paris"));
        when(apiClient.fetchWeather("London")).thenReturn("{\"weather\": {\"main\": \"Clouds\"}}");
        when(apiClient.fetchWeather("Paris")).thenReturn("{\"weather\": {\"main\": \"Clear\"}}");

        updater = new WeatherUpdater(cacheManager, apiClient, 1);
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(apiClient, atLeastOnce()).fetchWeather("London");
            verify(apiClient, atLeastOnce()).fetchWeather("Paris");
        });

        updater.stop();
    }

    @Test
    void testUpdaterHandlesExceptionsGracefully() {
        when(cacheManager.getCachedCities()).thenReturn(Set.of("New York"));
        when(apiClient.fetchWeather("New York")).thenThrow(new WeatherSdkException("API Error"));

        updater = new WeatherUpdater(cacheManager, apiClient, 1);
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(apiClient, atLeastOnce()).fetchWeather("New York"));

        updater.stop();
    }

    @Test
    void testUpdaterSkipsEmptyCache() {
        when(cacheManager.getCachedCities()).thenReturn(Set.of());
        updater = new WeatherUpdater(cacheManager, apiClient, 1);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(apiClient, never()).fetchWeather(anyString()));

        updater.stop();
    }

    @Test
    void testUpdaterChangesInterval() {
        when(cacheManager.getCachedCities()).thenReturn(Set.of("Tokyo"));
        when(apiClient.fetchWeather("Tokyo")).thenReturn("{\"weather\": {\"main\": \"Rain\"}}");

        updater = new WeatherUpdater(cacheManager, apiClient, 1);
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(apiClient, atLeastOnce()).fetchWeather("Tokyo"));

        updater.updateInterval(3);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(apiClient, atLeast(2)).fetchWeather("Tokyo"));

        updater.stop();
    }

    @Test
    void testUpdaterThreadSafety() throws InterruptedException {
        when(cacheManager.getCachedCities()).thenReturn(Set.of("Berlin"));
        when(apiClient.fetchWeather("Berlin")).thenReturn("{\"weather\": {\"main\": \"Sunny\"}}");

        updater = new WeatherUpdater(cacheManager, apiClient, 1);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++)
            executor.submit(() -> {
                updater.updateWeather();
                latch.countDown();
            });

        latch.await();
        executor.shutdown();
        verify(apiClient, atMost(5)).fetchWeather("Berlin");

        updater.stop();
    }

    @Test
    void testUpdaterDoesNotRunAfterStop() {
        when(cacheManager.getCachedCities()).thenReturn(Set.of("Madrid"));
        when(apiClient.fetchWeather("Madrid")).thenReturn("{\"weather\": {\"main\": \"Sunny\"}}");

        updater = new WeatherUpdater(cacheManager, apiClient, 1);
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(apiClient, atLeastOnce()).fetchWeather("Madrid"));

        updater.stop();
        await().during(2, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                verify(apiClient, times(1)).fetchWeather("Madrid")
        );
    }

    @Test
    void testUpdaterThrowsExceptionForInvalidInterval() {
        assertThrows(WeatherSdkException.class, () -> new WeatherUpdater(cacheManager, apiClient, 0));
        assertThrows(WeatherSdkException.class, () -> new WeatherUpdater(cacheManager, apiClient, -5));
    }

    private boolean isSchedulerShutdown(WeatherUpdater updater) {
        ScheduledExecutorService scheduler = extractScheduler(updater);
        return scheduler.isShutdown();
    }

    private ScheduledExecutorService extractScheduler(WeatherUpdater updater) {
        try {
            var field = WeatherUpdater.class.getDeclaredField("scheduler");
            field.setAccessible(true);
            return (ScheduledExecutorService) field.get(updater);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access scheduler field", e);
        }
    }
}