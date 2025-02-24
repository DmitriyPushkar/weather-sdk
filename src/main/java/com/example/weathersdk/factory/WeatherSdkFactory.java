package com.example.weathersdk.factory;

import com.example.weathersdk.api.WeatherSdk;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


// Factory for managing WeatherSdk instances
// Used to ensure that each API key has a unique instance
public final class WeatherSdkFactory {
    private static final Map<String, WeatherSdk> instances = new ConcurrentHashMap<>();

    private WeatherSdkFactory() {
    }

    public static WeatherSdk createInstance(String apiKey, int pollingInterval) {
        return null;
    }

    public static void deleteInstance(String apiKey) {
    }

    public static Optional<WeatherSdk> getInstance(String apiKey) {
        return Optional.ofNullable(instances.get(apiKey));
    }
}
