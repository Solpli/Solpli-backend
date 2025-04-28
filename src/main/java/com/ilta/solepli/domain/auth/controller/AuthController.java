package com.ilta.solepli.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.auth.dto.request.BasicLoginRequest;
import com.ilta.solepli.domain.auth.dto.response.LoginResponse;
import com.ilta.solepli.domain.auth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "인증 및 회원 관련 API")
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "기본 회원가입",
      description = "(관리자) 아이디와 비밀번호로로 회원가입합니다. 로그인에 사용할 아이디와 비밀번호를 입력해주세요.")
  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody BasicLoginRequest basicLoginRequest) {

    authService.signup(basicLoginRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Operation(
      summary = "기본 로그인",
      description = "(관리자) 아이디와 비밀번호로 로그인합니다. 로그인에 사용할 아이디와 비밀번호를 입력해주세요.")
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody BasicLoginRequest basicLoginRequest) {

    LoginResponse loginResponse = authService.login(basicLoginRequest);
    return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
  }
}
