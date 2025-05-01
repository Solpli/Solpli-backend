package com.ilta.solepli.domain.auth.dto.response.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(@JsonProperty("access_token") String accessToken) {}
