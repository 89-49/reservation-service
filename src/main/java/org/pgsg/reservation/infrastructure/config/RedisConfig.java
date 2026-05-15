package org.pgsg.reservation.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.geo.GeoModule;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Primary
    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 날짜 모듈 및 스프링 데이터 관련 공식 Jackson 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new GeoModule());

        // 🚨 기존의 부실했던 pageModule() 대신, 역직렬화가 완벽히 지원되는 PageJacksonModule을 강제 등록합니다.
        objectMapper.registerModule(new org.springframework.data.web.config.SpringDataJacksonConfiguration.PageJacksonModule());

        // 2. 날짜 배열 쪼개짐 방지 및 타임스탬프 비활성화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 3. 가시성 설정 (Jackson이 private 필드나 생성자에도 접근할 수 있도록 셋팅)
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // 4. 없는 필드가 역직렬화 대상에 있어도 무시하고 정상 파싱
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 5. 다형성 타이핑 범위 교정
        // 자바 표준 unmodifiable 리스트 등이 오염되어 터지는 걸 막기 위해,
        // 오직 우리가 만든 패키지("org.pgsg")와 프레임워크 필수 객체(Object)만 타입 정보를 붙이도록 가이드라인을 좁힙니다.
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("org.pgsg")
                .allowIfBaseType(Object.class)
                .build();

        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, // 최신 규격 반영
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonSerializer);

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory factory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .entryTtl(Duration.ofDays(1));

        RedisCacheConfiguration listCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .entryTtl(Duration.ofSeconds(30));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("reservations", listCacheConfig)
                .build();
    }
}