package com.ilta.solepli.domain.sollect.dto;

import com.querydsl.core.annotations.QueryProjection;

public record SollectSearchResponseContent(
    String thumbnailImage, String title, String district, String neighborhood) {
  @QueryProjection
  public SollectSearchResponseContent(
      String thumbnailImage, String title, String district, String neighborhood) {
    this.thumbnailImage = thumbnailImage;
    this.title = title;
    this.district = district;
    this.neighborhood = neighborhood;
  }
}
