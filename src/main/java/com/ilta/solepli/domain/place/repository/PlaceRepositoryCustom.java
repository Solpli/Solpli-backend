package com.ilta.solepli.domain.place.repository;

import java.util.List;

public interface PlaceRepositoryCustom {

  List<String> getTopTagsForPlace(Long placeId, int limit);

  List<String> getReviewThumbnails(Long placeId, int limit);

  Integer getRecommendationPercent(Long placeId);
}
