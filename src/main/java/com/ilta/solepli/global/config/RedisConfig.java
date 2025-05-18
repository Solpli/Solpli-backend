package com.ilta.solepli.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.RedisURI;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisProperties redisProperties;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {

    // Redis 서버의 호스트와 포트 지정
    RedisStandaloneConfiguration standalone =
        new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

    // RedisURI 빌더: SSL on/off, 인증서 검증 on/off 설정
    RedisURI uri =
        RedisURI.builder()
            .withHost(redisProperties.getHost())
            .withPort(redisProperties.getPort())
            .withSsl(redisProperties.isSslEnable())
            .withVerifyPeer(!redisProperties.isSkipSslVerify()) // true 면 검증, false 면 스킵
            .build();

    // RedisURI 설정을 그대로 적용
    LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder().apply(uri).build();

    return new LettuceConnectionFactory(standalone, clientConfig);
  }

  @Bean
  public RedisTemplate<String, Object> recentSearchTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    // KEY는 String
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());

    // VALUE도 String
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new StringRedisSerializer());

    return redisTemplate;
  }
}
