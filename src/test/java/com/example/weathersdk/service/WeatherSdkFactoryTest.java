package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.exception.InvalidApiKeyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherSdkFactoryTest {

    @Test
    void testFactoryCreatesSingleInstancePerApiKey() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("test-key", 0);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("test-key", 0);

        assertSame(sdk1, sdk2, "Factory should return the same instance for the same API key");

        WeatherSdkFactory.removeInstance("test-key");
    }

    @Test
    void testFactoryCreatesDifferentInstancesForDifferentKeys() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("key1", 0);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("key2", 0);

        assertNotSame(sdk1, sdk2, "Factory should create different instances for different API keys");

        WeatherSdkFactory.removeInstance("key1");
        WeatherSdkFactory.removeInstance("key2");
    }

    @Test
    void testRemoveInstanceDeletesInstance() {
        WeatherSdk sdk = WeatherSdkFactory.getInstance("removable-key", 0);
        assertDoesNotThrow(() -> WeatherSdkFactory.removeInstance("removable-key"));

        WeatherSdk newSdk = WeatherSdkFactory.getInstance("removable-key", 0);
        assertNotSame(sdk, newSdk, "Factory should create a new instance after removal");

        WeatherSdkFactory.removeInstance("removable-key");
    }

    @Test
    void testRemoveNonExistentInstanceThrowsException() {
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.removeInstance("non-existent-key"));
    }

    @Test
    void testFactoryRejectsInvalidApiKey() {
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance("", 0));
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance("   ", 10));
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance(null, 5));
    }
}