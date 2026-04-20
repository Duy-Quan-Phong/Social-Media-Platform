package com.codegym.socialmedia.component;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory sliding-window rate limiter.
 * One counter per (key, window). Window resets after WINDOW_MS.
 */
@Component
public class RateLimitService {

    private static final long WINDOW_MS = 60_000L; // 1 minute

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    /**
     * @param key     unique key (e.g. "comment:127.0.0.1")
     * @param limit   max requests allowed per window
     * @return true if the request is allowed
     */
    public boolean isAllowed(String key, int limit) {
        long now = System.currentTimeMillis();
        long start = windowStart.getOrDefault(key, 0L);

        if (now - start > WINDOW_MS) {
            windowStart.put(key, now);
            counters.put(key, new AtomicInteger(1));
            return true;
        }

        return counters.computeIfAbsent(key, k -> new AtomicInteger(0))
                       .incrementAndGet() <= limit;
    }
}
