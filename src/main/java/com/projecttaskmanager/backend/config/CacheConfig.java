//package com.projecttaskmanager.backend.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.*;
//import org.springframework.data.redis.cache.*;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.*;
//
//import java.time.Duration;
//
//@Configuration
//@EnableCaching
//@RequiredArgsConstructor
//public class CacheConfig {
//
//    private final GenericJackson2JsonRedisSerializer redisSerializer;
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
//
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .prefixCacheNameWith("cache-redis:")
//                .entryTtl(Duration.ofMinutes(10))
//                .disableCachingNullValues()
//                .serializeKeysWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
//                )
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
//                );
//
//        return RedisCacheManager.builder(factory)
//                .cacheDefaults(config)
//                .build();
//    }
//}