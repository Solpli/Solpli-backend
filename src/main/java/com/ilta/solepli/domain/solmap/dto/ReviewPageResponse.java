package com.ilta.solepli.domain.solmap.dto;

import java.util.List;

public record ReviewPageResponse(List<ReviewDetail> reviews, Long nextCursor) {
  public static ReviewPageResponse of(List<ReviewDetail> reviews, Long nextCursor) {
    return new ReviewPageResponse(reviews, nextCursor);
  }
}
