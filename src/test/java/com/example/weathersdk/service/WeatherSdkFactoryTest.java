package com.example.weathersdk.service;

import com.example.weathersdk.api.WeatherSdk;
import com.example.weathersdk.enums.UpdateMode;
import com.example.weathersdk.exception.InvalidApiKeyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherSdkFactoryTest {

    @Test
    void testFactoryCreatesSingleInstancePerApiKey() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("test-key", UpdateMode.POLLING, 10);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("test-key", UpdateMode.POLLING, 10);

        assertSame(sdk1, sdk2, "Factory should return the same instance for the same API key");

        WeatherSdkFactory.removeInstance("test-key");
    }

    @Test
    void testFactoryOverwritesOldPollingIntervalValueWithNewOneForApiKey() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("test-key", UpdateMode.POLLING, 10);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("test-key", UpdateMode.POLLING, 15);

        assertNotSame(sdk1, sdk2, "Factory should return a different instance for the same API key");

        WeatherSdkFactory.removeInstance("test-key");
    }

    @Test
    void testFactoryOverwritesOldModeValueWithNewOneForApiKey() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("test-key", UpdateMode.POLLING, 10);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("test-key", UpdateMode.ON_DEMAND, 0);

        assertNotSame(sdk1, sdk2, "Factory should return a different instance for the same API key");

        WeatherSdkFactory.removeInstance("test-key");
    }

    @Test
    void testFactoryCreatesDifferentInstancesForDifferentKeys() {
        WeatherSdk sdk1 = WeatherSdkFactory.getInstance("key1", UpdateMode.ON_DEMAND, 0);
        WeatherSdk sdk2 = WeatherSdkFactory.getInstance("key2", UpdateMode.ON_DEMAND, 0);

        assertNotSame(sdk1, sdk2, "Factory should create different instances for different API keys");

        WeatherSdkFactory.removeInstance("key1");
        WeatherSdkFactory.removeInstance("key2");
    }

    @Test
    void testRemoveInstanceDeletesInstance() {
        WeatherSdk sdk = WeatherSdkFactory.getInstance("removable-key", UpdateMode.ON_DEMAND, 0);
        assertDoesNotThrow(() -> WeatherSdkFactory.removeInstance("removable-key"));

        WeatherSdk newSdk = WeatherSdkFactory.getInstance("removable-key", UpdateMode.ON_DEMAND, 0);
        assertNotSame(sdk, newSdk, "Factory should create a new instance after removal");

        WeatherSdkFactory.removeInstance("removable-key");
    }

    @Test
    void testRemoveNonExistentInstanceThrowsException() {
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.removeInstance("non-existent-key"));
    }

    @Test
    void testFactoryRejectsInvalidApiKey() {
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance("", UpdateMode.ON_DEMAND, 0));
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance("   ", UpdateMode.ON_DEMAND, 10));
        assertThrows(InvalidApiKeyException.class, () -> WeatherSdkFactory.getInstance(null, UpdateMode.ON_DEMAND, 5));
    }
}