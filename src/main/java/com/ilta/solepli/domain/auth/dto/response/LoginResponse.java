package com.ilta.solepli.domain.auth.dto.response;

import lombok.Builder;

import com.ilta.solepli.domain.user.entity.Role;

@Builder
public record LoginResponse(String accessToken, Role role) {
  public static LoginResponse from(String accessToken, Role role) {
    return LoginResponse.builder().accessToken(accessToken).role(role).build();
  }
}
