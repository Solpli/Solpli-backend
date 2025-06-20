package com.ilta.solepli.domain.solmark.sollect.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record SolmarkSollectResponse(List<SollectSearchContent> contents, CursorInfo cursorInfo) {
  @Builder
  public record SollectSearchContent(
      Long sollectId, String thumbnailImage, String title, String district, String neighborhood) {}

  @Builder
  public record CursorInfo(Long nextCursorId, boolean hasNext) {}
}
