package com.ilta.solepli.domain.auth.dto.response.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverTokenResponse(@JsonProperty("access_token") String accessToken) {}
