package com.codegym.socialmedia.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Simple in-memory cache; replace with Redis/Caffeine for production scale
        return new ConcurrentMapCacheManager("users", "userStats", "friendships", "friendCounts");
    }
}
