package com.example.weathersdk.service;

import com.example.weathersdk.exception.WeatherSdkException;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class WeatherUpdater {
    private final WeatherCacheManager cacheManager;
    private final OpenWeatherApiClient apiClient;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile int interval;
    private ScheduledExecutorService scheduler;

    public WeatherUpdater(WeatherCacheManager cacheManager, OpenWeatherApiClient apiClient, int interval) {
        if (interval <= 0) throw new WeatherSdkException("Polling interval must be greater than 0 seconds.");
        this.cacheManager = cacheManager;
        this.apiClient = apiClient;
        this.interval = interval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateWeather, 0, this.interval, TimeUnit.SECONDS);
        log.info("WeatherUpdater started with polling interval of {} seconds.", this.interval);
    }

    public synchronized void updateInterval(int newInterval) {
        if (newInterval <= 0) throw new WeatherSdkException("Polling interval must be greater than 0 seconds.");
        log.info("Updating polling interval to {} seconds.", newInterval);

        ScheduledExecutorService oldScheduler = this.scheduler;
        if (oldScheduler != null) {
            oldScheduler.shutdown();
            try {
                if (!oldScheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    log.warn("Previous scheduler did not terminate in time, forcing shutdown.");
                    oldScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Shutdown interrupted, forcing termination.");
                oldScheduler.shutdownNow();
            }
        }

        this.interval = newInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateWeather, 0, newInterval, TimeUnit.SECONDS);
        log.info("Polling interval updated successfully to {} seconds.", newInterval);
    }

    public synchronized void stop() {
        if (scheduler != null) {
            log.info("Stopping WeatherUpdater...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Scheduler did not terminate in time, forcing shutdown.");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Shutdown interrupted, forcing termination.");
                scheduler.shutdownNow();
            }
            log.info("WeatherUpdater stopped.");
        }
    }

    void updateWeather() {
        if (!lock.tryLock()) {
            log.warn("Skipping update, another update is already in progress.");
            return;
        }

        try {
            log.debug("Fetching cached cities...");
            Set<String> cities = cacheManager.getCachedCities();
            if (cities.isEmpty()) {
                log.info("No cities in cache, skipping update.");
                return;
            }

            log.info("Updating weather data for {} cities.", cities.size());
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(cities.size(), 5));
            CountDownLatch latch = new CountDownLatch(cities.size());

            for (String city : cities) {
                executor.submit(() -> {
                    try {
                        String weatherData = apiClient.fetchWeather(city);
                        cacheManager.updateCache(city, weatherData);
                        log.info("Successfully updated weather for '{}'.", city);
                    } catch (Exception e) {
                        log.error("Failed to update weather for '{}': {}", city, e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();
            log.info("Weather update cycle completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Weather update thread was interrupted.", e);
        } finally {
            lock.unlock();
        }
    }
}