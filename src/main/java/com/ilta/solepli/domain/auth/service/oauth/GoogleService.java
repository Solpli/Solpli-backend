package com.ilta.solepli.domain.auth.service.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import com.ilta.solepli.domain.auth.dto.response.oauth.google.GoogleTokenResponse;
import com.ilta.solepli.domain.auth.dto.response.oauth.google.GoogleUserInfoResponse;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class GoogleService implements OAuthService {

  private final WebClient webClient;
  private static final String TOKEN_REQUEST_URI = "https://oauth2.googleapis.com/token";
  private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

  @Value("${GOOGLE_CLIENT_ID}")
  private String googleClientId;

  @Value("${GOOGLE_CLIENT_SECRET}")
  private String googleClientSecret;

  @Value("${GOOGLE_REDIRECT_URI}")
  private String googleRedirectUri;

  @Override
  public String getAccessToken(String code) {
    String requestBody =
        "grant_type=authorization_code"
            + "&client_id="
            + googleClientId
            + "&client_secret="
            + googleClientSecret
            + "&redirect_uri="
            + googleRedirectUri
            + "&code="
            + code;

    return webClient
        .post()
        .uri(TOKEN_REQUEST_URI)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(requestBody)
        .exchangeToMono(
            response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(GoogleTokenResponse.class);
              } else {
                return response
                    .bodyToMono(String.class)
                    .flatMap(
                        errorBody ->
                            Mono.error(
                                new CustomException(ErrorCode.GET_GOOGLE_ACCESS_TOKEN_FAILED)));
              }
            })
        .map(GoogleTokenResponse::accessToken)
        .block();
  }

  @Override
  public String getLoginId(String accessToken) {
    return webClient
        .get()
        .uri(USER_INFO_URI)
        .header("Authorization", "Bearer " + accessToken.trim())
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            clientResponse ->
                Mono.error(new CustomException(ErrorCode.GET_GOOGLE_UNIQUE_ID_FAILED)))
        .bodyToMono(GoogleUserInfoResponse.class)
        .map(response -> response.sub())
        .block();
  }
}
