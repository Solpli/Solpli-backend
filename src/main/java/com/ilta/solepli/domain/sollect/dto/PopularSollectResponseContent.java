package com.ilta.solepli.domain.sollect.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PopularSollectResponseContent(
    Long sollectId,
    String thumbnailImage,
    String title,
    String placeName,
    String district,
    String neighborhood) {
  @QueryProjection
  public PopularSollectResponseContent(
      Long sollectId,
      String thumbnailImage,
      String title,
      String placeName,
      String district,
      String neighborhood) {
    this.sollectId = sollectId;
    this.thumbnailImage = thumbnailImage;
    this.title = title;
    this.placeName = placeName;
    this.district = district;
    this.neighborhood = neighborhood;
  }
}
