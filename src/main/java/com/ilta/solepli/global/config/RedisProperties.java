package com.ilta.solepli.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
  private String host; // Redis 서버 IP
  private int port; // Redis 서버 포트
  private boolean sslEnable; // TLS 암호화 사용 여부
  private boolean skipSslVerify; // TLS 연결 시 인증서 검증 스킵 여부
}
