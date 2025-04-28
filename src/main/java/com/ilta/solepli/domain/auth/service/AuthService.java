package com.ilta.solepli.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.auth.dto.request.BasicLoginRequest;
import com.ilta.solepli.domain.auth.dto.response.LoginResponse;
import com.ilta.solepli.domain.user.entity.Role;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.repository.UserRepository;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public void signup(BasicLoginRequest request) {
    String loginId = request.loginId();

    if (userRepository.existsByLoginId(loginId)) {
      throw new CustomException(ErrorCode.USER_EXISTS);
    }

    userRepository.save(
        User.builder()
            .role(Role.ADMIN)
            .loginId(loginId)
            .password(passwordEncoder.encode(request.password()))
            .build());
  }

  @Transactional(readOnly = true)
  public LoginResponse login(BasicLoginRequest request) {
    User user =
        userRepository
            .findByLoginId(request.loginId())
            .orElseThrow(() -> new CustomException(ErrorCode.INCORRECT_ACCOUNT));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new CustomException(ErrorCode.INCORRECT_ACCOUNT);
    }

    String accessToken = jwtTokenProvider.generateToken(user);

    return LoginResponse.from(accessToken, user.getRole());
  }
}
