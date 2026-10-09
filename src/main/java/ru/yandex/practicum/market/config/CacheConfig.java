package ru.yandex.practicum.market.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager shortTtlCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper

    ) {
        ObjectMapper cacheObjectMapper = objectMapper.rebuild()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("ru.yandex.practicum.market")
                                .allowIfSubType("java")
                                .build(),
                        DefaultTyping.NON_FINAL_AND_RECORDS,
                        JsonTypeInfo.As.PROPERTY
                ).build();

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(30))
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJacksonJsonRedisSerializer(cacheObjectMapper)
                        ))
                ;

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Primary
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        // 1. Создаем копию ObjectMapper, чтобы не сломать глобальные настройки приложения
        ObjectMapper cacheObjectMapper = objectMapper.rebuild()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("ru.yandex.practicum.market")
                                .allowIfSubType("java")
                                .build(),
                DefaultTyping.NON_FINAL_AND_RECORDS,
                JsonTypeInfo.As.PROPERTY
        ).build();


        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJacksonJsonRedisSerializer(cacheObjectMapper)
                        ))
                ;

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

}
