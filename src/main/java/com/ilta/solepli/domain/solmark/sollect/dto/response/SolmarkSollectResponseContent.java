package com.ilta.solepli.domain.solmark.sollect.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record SolmarkSollectResponseContent(
    Long sollectId, String thumbnailImage, String title, String district, String neighborhood) {
  @QueryProjection
  public SolmarkSollectResponseContent(
      Long sollectId, String thumbnailImage, String title, String district, String neighborhood) {
    this.sollectId = sollectId;
    this.thumbnailImage = thumbnailImage;
    this.title = title;
    this.district = district;
    this.neighborhood = neighborhood;
  }
}
