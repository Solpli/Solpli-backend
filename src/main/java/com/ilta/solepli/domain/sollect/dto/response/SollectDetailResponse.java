package com.ilta.solepli.domain.sollect.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import com.ilta.solepli.domain.sollect.entity.ContentType;

@Builder
public record SollectDetailResponse(
    String thumbnailImageUrl,
    String title,
    String placeName,
    Integer otherPlaceCount,
    String district,
    String neighborhood,
    String profileImageUrl,
    String nickname,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
    List<SollectContent> contents,
    Long savedCount,
    List<PlaceSummary> placeSummaries) {
  @Builder
  public record SollectContent(ContentType type, String imageUrl, String text) {}

  @Builder
  public record PlaceSummary(
      String name,
      String category,
      Integer recommendationRate,
      List<String> tags,
      Boolean isSaved,
      Double rating) {}
}
