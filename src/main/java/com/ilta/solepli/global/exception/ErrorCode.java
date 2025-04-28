package com.ilta.solepli.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // 에러코드 예시: 샘플 에러
  SAMPLE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예시: 샘플 에러가 발생했습니다."),

  // 인증 관련 에러
  INCORRECT_ACCOUNT(HttpStatus.BAD_REQUEST, "해당 계정이 존재하지 않습니다."),
  INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),

  // 유저 관련 에러
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보가 존재하지 않습니다."),
  USER_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 계정입니다."),

  // validation
  LOGIN_ID_REQUIRED(HttpStatus.BAD_REQUEST, "아이디는 필수입니다."),
  PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String message) {

    this.httpStatus = httpStatus;
    this.message = message;
  }
}
