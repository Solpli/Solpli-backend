package com.ilta.solepli.global.util;

public class PlaceUtil {

  /** 두 위경도 좌표 간의 거리를 계산하여 미터 단위(double)로 반환. */
  public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
    final int EARTH_RADIUS = 6371000; // 지구 반지름 (미터 단위)

    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c; // 결과: 미터(m) 단위 거리
  }

  public static Double truncateTo2Decimals(Double num) {
    if (num == null) {
      return null;
    }
    // 소수점 첫째 자리(0.1 단위)까지만 남기고 그 이하 버림
    return Math.floor(num * 10) / 10.0;
  }
}
