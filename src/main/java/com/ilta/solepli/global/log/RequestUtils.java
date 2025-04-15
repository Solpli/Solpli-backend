package com.ilta.solepli.global.log;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

  /**
   * 클라이언트의 IP 주소를 반환한다.
   *
   * <p>우선 HTTP 헤더 "X-Forwarded-For"가 있을 경우 해당 값을 사용하고, 없으면 HttpServletRequest의 getRemoteAddr() 메서드를
   * 통해 원격 IP 주소를 리턴한다.
   */
  public static String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded;
    }
    return request.getRemoteAddr();
  }

  /**
   * 요청에 포함된 모든 HTTP 헤더 정보를 Map 형태로 수집하여 문자열로 반환한다.
   *
   * <p>HttpServletRequest 객체에서 헤더 이름을 Enumeration으로 가져온 후, 각 헤더 이름과 값을 Map에 저장하고, 이를 문자열로 변환한다.
   */
  public static String getRequestHeaders(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames == null) {
      return "";
    }
    Map<String, String> headersMap = new HashMap<>();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      headersMap.put(headerName, headerValue);
    }
    return headersMap.toString();
  }

  /**
   * /** 요청 파라미터들을 문자열로 변환하여 반환한다.
   *
   * <p>HttpServletRequest 객체의 파라미터 맵을 이용해 각 엔트리를 "키=[값1, 값2, ...]" 형태로 변환하고, 여러 파라미터는 쉼표(,)로 구분하여
   * 하나의 문자열로 연결한다.
   */
  public static String getRequestParams(HttpServletRequest request) {
    Map<String, String[]> paramMap = request.getParameterMap();
    if (paramMap.isEmpty()) {
      return "";
    }
    return paramMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
        .collect(Collectors.joining(", "));
  }

  /**
   * ContentCachingRequestWrapper를 사용하여 요청 본문(body) 데이터를 문자열로 추출한다.
   *
   * <p>요청에서 바이트 배열 형태로 저장된 본문 데이터를, 지정된 문자 인코딩 방식에 따라 문자열로 변환한다. 변환 도중 문제가 발생하면 에러 메시지를 반환한다.
   */
  public static String getRequestBody(ContentCachingRequestWrapper requestWrapper) {
    byte[] buf = requestWrapper.getContentAsByteArray();
    if (buf.length > 0) {
      try {
        return new String(buf, 0, buf.length, requestWrapper.getCharacterEncoding());
      } catch (Exception ex) {
        return "[error reading request body]";
      }
    }
    return "";
  }

  /**
   * ContentCachingResponseWrapper를 사용하여 응답 본문(body) 데이터를 문자열로 추출한다.
   *
   * <p>응답의 바이트 배열 데이터를 UTF-8 인코딩으로 문자열로 변환하며, 변환 도중 오류가 발생할 경우 에러 메시지를 반환한다.
   */
  public static String getResponseBody(ContentCachingResponseWrapper responseWrapper) {
    byte[] buf = responseWrapper.getContentAsByteArray();
    if (buf.length > 0) {
      try {
        return new String(buf, 0, buf.length, "UTF-8");
      } catch (Exception ex) {
        return "[error reading response body]";
      }
    }
    return "";
  }
}
