package com.ilta.solpli.global;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
// @Tag(name = "Server Health", description = "서버가 정상 작동하는지 확인")
public class HealthCheck {

  @GetMapping
  // @Operation(summary = "헬스 체크 API", description = "서버가 정상 작동하는지 확인합니다.")
  public String check() {
    return "OK";
  }

  @GetMapping("/test")
  // @Operation(summary = "CI/CD API", description = "CI/CD 테스트 API")
  public String cicdTest() {
    LocalDateTime localDateTime = LocalDateTime.now();
    return "test9 : " + localDateTime;
  }
}
