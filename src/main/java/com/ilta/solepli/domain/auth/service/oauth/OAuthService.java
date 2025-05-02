package com.ilta.solepli.domain.auth.service.oauth;

public interface OAuthService {
  String getAccessToken(String code);

  String getLoginId(String accessToken);
}
