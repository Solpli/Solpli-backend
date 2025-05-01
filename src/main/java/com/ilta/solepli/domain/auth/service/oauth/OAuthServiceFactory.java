package com.ilta.solepli.domain.auth.service.oauth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ilta.solepli.domain.auth.entity.LoginType;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service // 팩토리 패턴 사용해서 LoginType 맞는 serivce 리턴
public class OAuthServiceFactory {
  private final Map<LoginType, OAuthService> oauthServices;

  public OAuthServiceFactory(List<OAuthService> oauthServiceList) {
    this.oauthServices =
        oauthServiceList.stream()
            .collect(Collectors.toMap(service -> getLoginType(service), service -> service));
  }

  private LoginType getLoginType(OAuthService service) {
    if (service instanceof KakaoService) {
      return LoginType.KAKAO;
    } else if (service instanceof NaverService) {
      return LoginType.NAVER;
    }
    //        else if(service instanceof GoogleService) {
    //            return LoginType.GOOGLE;
    //        }
    //        else if (service instanceof BasicService) {
    //            return LoginType.BASIC;
    //        }
    throw new CustomException(ErrorCode.INCORRECT_LOGIN_TYPE);
  }

  public OAuthService getOAuthService(LoginType loginType) {
    return oauthServices.get(loginType);
  }
}
