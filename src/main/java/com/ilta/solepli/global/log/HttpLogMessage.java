package com.ilta.solepli.global.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;

@Getter
public class HttpLogMessage {

  private final String httpMethod;
  private final String requestUri;
  private final HttpStatus httpStatus;
  private final String clientIp;
  private final double elapsedTime;
  private final String headers;
  private final String requestParam;
  private final String requestBody;
  private final String responseBody;
  private final String timestamp;

  public HttpLogMessage(
      String httpMethod,
      String requestUri,
      HttpStatus httpStatus,
      String clientIp,
      double elapsedTime,
      String headers,
      String requestParam,
      String requestBody,
      String responseBody,
      String timestamp) {

    this.httpMethod = httpMethod;
    this.requestUri = requestUri;
    this.httpStatus = httpStatus;
    this.clientIp = clientIp;
    this.elapsedTime = elapsedTime;
    this.headers = headers;
    this.requestParam = requestParam;
    this.requestBody = requestBody;
    this.responseBody = responseBody;
    this.timestamp = timestamp;
  }

  public static HttpLogMessage createInstance(
      ContentCachingRequestWrapper requestWrapper,
      ContentCachingResponseWrapper responseWrapper,
      double elapsedTime) {

    String method = requestWrapper.getMethod();
    String uri = requestWrapper.getRequestURI();
    HttpStatus status = HttpStatus.valueOf(responseWrapper.getStatus());
    String clientIp = RequestUtils.getClientIp(requestWrapper);
    String headers = RequestUtils.getRequestHeaders(requestWrapper);
    String requestParam = RequestUtils.getRequestParams(requestWrapper);
    String requestBody = RequestUtils.getRequestBody(requestWrapper);
    String responseBody = RequestUtils.getResponseBody(responseWrapper);
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    return new HttpLogMessage(
        method,
        uri,
        status,
        clientIp,
        elapsedTime,
        headers,
        requestParam,
        requestBody,
        responseBody,
        timestamp);
  }

  public String toPrettierLog() {

    String prettyResponse = responseBody;
    try {
      // JSON 문자열 파싱 및 pretty-print
      ObjectMapper mapper = new ObjectMapper();
      Object json = mapper.readValue(responseBody, Object.class);
      ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
      prettyResponse = writer.writeValueAsString(json);
    } catch (Exception e) {
      // 예외 발생 시 그냥 원본 문자열 사용
      prettyResponse = responseBody;
    }

    return String.format(
        "%n=============================(START)%n"
            + "Timestamp      : %s%n"
            + "[REQUEST]      : %s %s %s (%.3fs)%n"
            + "-----------------------------%n"
            + "Client IP      : %s%n"
            + "Headers        : %s%n"
            + "Request Params : %s%n"
            + "Request Body   : %s%n"
            + "Response Body  : %s%n"
            + "=============================(END)",
        timestamp,
        httpMethod,
        requestUri,
        httpStatus,
        elapsedTime,
        clientIp,
        headers,
        requestParam,
        requestBody,
        prettyResponse);
  }
}
