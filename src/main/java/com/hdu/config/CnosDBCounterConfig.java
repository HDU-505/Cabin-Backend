package com.hdu.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CnosDBCounterConfig {
    private ConcurrentHashMap<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    public int incrementAndGet(String tableName) {
        return counterMap.computeIfAbsent(tableName, key -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getCurrentCount(String tableName) {
        return counterMap.getOrDefault(tableName, new AtomicInteger(0)).get();
    }
}
