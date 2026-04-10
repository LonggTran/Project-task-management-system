package com.projecttaskmanager.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;

        return objectMapper.convertValue(value, clazz);
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;

        return objectMapper.convertValue(value, typeReference);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public boolean exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    public Long getTtl(String key) {
        return redisTemplate.getExpire(key);
    }

    public <T> T getOrLoad(String key, TypeReference<T> typeReference, Supplier<T> dbCall, long timeout,
                           TimeUnit unit) {
        // 1. GET cache
        T data = get(key, typeReference);
        if (data != null) return data;
        // 2. MISS → DB
        data = dbCall.get();
        // 3. SET cache
        if (data != null) {
            set(key, data, timeout, unit);
        }

        return data;
    }

    public <T> T getOrLoad(String key,
                           Class<T> clazz,
                           Supplier<T> dbCall,
                           long timeout,
                           TimeUnit unit) {

        T data = get(key, clazz);
        if (data != null) return data;

        data = dbCall.get();

        if (data != null) {
            set(key, data, timeout, unit);
        }

        return data;
    }
}