package com.ilta.solepli.domain.auth.dto.response.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfoResponse(
    @JsonProperty("resultcode") String resultCode,
    @JsonProperty("message") String message,
    @JsonProperty("response") NaverUserInfo naverUserInfo) {
  public record NaverUserInfo(String id) {}
}
