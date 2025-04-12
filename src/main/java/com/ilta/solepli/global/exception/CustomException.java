package com.ilta.solepli.global.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CustomException extends RuntimeException {

  public ErrorCode errorCode;

  public ErrorCode getErrorCode() {

    return errorCode;
  }

  public CustomException(ErrorCode errorCode) {

    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
