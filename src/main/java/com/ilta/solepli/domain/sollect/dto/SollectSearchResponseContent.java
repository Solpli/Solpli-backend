package com.ilta.solepli.domain.sollect.dto;

import com.querydsl.core.annotations.QueryProjection;

public record SollectSearchResponseContent(
    Long sollectId, String thumbnailImage, String title, String district, String neighborhood) {
  @QueryProjection
  public SollectSearchResponseContent(
      Long sollectId, String thumbnailImage, String title, String district, String neighborhood) {
    this.sollectId = sollectId;
    this.thumbnailImage = thumbnailImage;
    this.title = title;
    this.district = district;
    this.neighborhood = neighborhood;
  }
}
