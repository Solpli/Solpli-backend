package com.ilta.solepli.domain.sollect.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record SollectSearchResponse(List<SollectSearchContent> contents, CursorInfo cursorInfo) {
  @Builder
  public record SollectSearchContent(
      Long sollectId,
      String thumbnailImage,
      String title,
      String district,
      String neighborhood,
      Boolean isMarked) {}

  @Builder
  public record PopularSollectContent(
      Long sollectId,
      String thumbnailImage,
      String title,
      String placeName,
      String district,
      String neighborhood,
      Boolean isMarked) {}

  @Builder
  public record CursorInfo(Long nextCursorId, boolean hasNext) {}
}
