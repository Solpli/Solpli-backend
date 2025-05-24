package com.ilta.solepli.domain.sollect.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record SollectSearchResponse(List<SollectSearchContent> contents, PageInfo pageInfo) {
  @Builder
  public record SollectSearchContent(
      String thumbnailImage, String title, String district, String neighborhood) {}

  @Builder
  public record PageInfo(int page, int size, int totalPages, long totalElements, boolean isLast) {}
}
