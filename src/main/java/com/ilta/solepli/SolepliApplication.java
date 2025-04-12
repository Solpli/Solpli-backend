package com.ilta.solepli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SolepliApplication {

  public static void main(String[] args) {
    SpringApplication.run(SolepliApplication.class, args);
  }
}
