package com.ilta.solepli.global.util;

import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;

public class SecurityUtil {
  // 비로그인은 null 반환, 로그인 사용자는 User 객체 반환
  public static User getUser(CustomUserDetails customUserDetails) {
    if (customUserDetails == null) {
      return null;
    }

    return customUserDetails.user();
  }
}
