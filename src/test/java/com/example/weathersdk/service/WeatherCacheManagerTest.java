package com.example.weathersdk.service;

import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.InvalidCityException;
import com.example.weathersdk.exception.WeatherSdkException;
import com.example.weathersdk.service.util.FakeTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class WeatherCacheManagerTest {
    private WeatherCacheManager cacheManager;
    private FakeTicker testTicker;

    @BeforeEach
    void setUp() {
        testTicker = new FakeTicker();
        cacheManager = new WeatherCacheManager(testTicker);
    }

    @Test
    void testAddAndGetData() {
        cacheManager.updateCache("London", "Sunny 15 degrees");
        assertEquals("Sunny 15 degrees", cacheManager.getCachedData("London"));
    }

    @Test
    void testEmptyWeatherDataThrowsException() {
        assertThrows(WeatherSdkException.class, () -> cacheManager.updateCache("New York", ""));
    }

    @Test
    void testAddMultipleCitiesAndEviction() {
        for (int i = 1; i <= 12; i++) {
            cacheManager.updateCache("City" + i, "WeatherData" + i);
        }

        assertThrows(CityNotFoundException.class, () -> cacheManager.getCachedData("City1"));
        assertThrows(CityNotFoundException.class, () -> cacheManager.getCachedData("City2"));
        assertNotNull(cacheManager.getCachedData("City3"));
        assertNotNull(cacheManager.getCachedData("City12"));
    }

    @Test
    void testCacheExpirationWithFakeTicker() {
        cacheManager.updateCache("Madrid", "Sunny 15 degrees");
        testTicker.advance(Duration.ofMinutes(11));
        assertThrows(CityNotFoundException.class, () -> cacheManager.getCachedData("Madrid"));
    }

    @Test
    void testConcurrencyWith50Threads() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int cityIndex = i;
            executor.execute(() -> {
                try {
                    cacheManager.updateCache("City" + cityIndex, "Weather" + cityIndex);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        assertTrue(cacheManager.getCachedCities().size() <= 10);
    }

    @Test
    void testUpdateExistingCity() {
        cacheManager.updateCache("Tokyo", "Cloudy 20 degrees");
        cacheManager.updateCache("Tokyo", "Sunny 25 degrees");

        assertEquals("Sunny 25 degrees", cacheManager.getCachedData("Tokyo"));
    }

    @Test
    void testCityNotFoundThrowsException() {
        assertThrows(CityNotFoundException.class, () -> cacheManager.getCachedData("NonExistingCity"));
    }

    @Test
    void testInvalidCityNameThrowsException() {
        assertThrows(InvalidCityException.class, () -> cacheManager.updateCache(null, "Weather"));
        assertThrows(InvalidCityException.class, () -> cacheManager.updateCache("  ", "Weather"));
        assertThrows(InvalidCityException.class, () -> cacheManager.getCachedData("  "));
    }
}