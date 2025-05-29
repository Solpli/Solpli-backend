package com.ilta.solepli.domain.place.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.sollect.dto.response.PlaceSearchResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectPlaceAddPreviewResponse;

@Repository
public interface PlaceRepositoryCustom {

  List<String> getTopTagsForPlace(Long placeId, int limit);

  List<String> getReviewThumbnails(Long placeId, int limit);

  Integer getRecommendationPercent(Long placeId);

  List<PlaceSearchResponse> getPlacesByKeyword(String keyword);

  SollectPlaceAddPreviewResponse getSollectAddPreview(Long placeId);
}
