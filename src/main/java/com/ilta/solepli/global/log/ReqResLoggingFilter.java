package com.ilta.solepli.global.log;

import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqResLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(ReqResLoggingFilter.class);
  public static final String REQUEST_ID = "request_id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    // 요청/응답을 캐싱용 Wrapper로 감싸기
    ContentCachingRequestWrapper cachingRequestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper cachingResponseWrapper =
        new ContentCachingResponseWrapper(response);

    // UUID를 사용해 요청마다 고유 ID 할당 (MDC에 저장)
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put(REQUEST_ID, requestId);

    long startTime = System.currentTimeMillis();
    try {
      // 체인을 통해 다음 필터 또는 서블릿으로 요청 전달
      filterChain.doFilter(cachingRequestWrapper, cachingResponseWrapper);
    } catch (Exception ex) {
      log.error("Exception during filtering", ex);
    } finally {
      // 요청 처리 종료 시간 및 소요 시간 계산 (초 단위)
      long endTime = System.currentTimeMillis();
      double elapsedTime = (endTime - startTime) / 1000.0;

      try {
        // 캐싱된 요청 및 응답 데이터를 활용하여 로그 메시지 작성
        HttpLogMessage logMessage =
            HttpLogMessage.createInstance(
                cachingRequestWrapper, cachingResponseWrapper, elapsedTime);
        log.info(logMessage.toPrettierLog());
      } catch (Exception ex) {
        log.error("[ReqResLoggingFilter] Logging failed", ex);
      } finally {
        // MDC에서 request id 제거
        MDC.remove(REQUEST_ID);
        try {
          // 캐싱된 응답 본문을 실제 응답 객체에 복사하여 클라이언트에 전달
          cachingResponseWrapper.copyBodyToResponse();
        } catch (Exception e) {
          log.error("Error copying body to response", e);
        }
      }
    }
  }
}
