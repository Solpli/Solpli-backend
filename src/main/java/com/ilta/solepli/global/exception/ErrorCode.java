package com.ilta.solepli.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // 에러코드 예시: 샘플 에러
  SAMPLE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예시: 샘플 에러가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String message) {

    this.httpStatus = httpStatus;
    this.message = message;
  }
}
