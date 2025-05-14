package com.ilta.solepli.domain.solmap.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.category.repository.CategoryRepository;
import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solmap.dto.ViewportMapMarkerDetail;
import com.ilta.solepli.domain.solmap.dto.ViewportMapMarkerResponse;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolmapService {

  private final PlaceRepository placeRepository;
  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public ViewportMapMarkerResponse getMarkersByViewport(
      Double swLat, Double swLng, Double neLat, Double neLng, String category) {

    // 좌표, 카테고리 유효성 검증
    validViewport(swLat, swLng, neLat, neLng);
    validCategory(category);

    // 좌표에 속한 장소 조회
    List<Place> place =
        placeRepository.findInViewportWithOptionalCategory(swLat, swLng, neLat, neLng, category);

    // 마커 관련 데이터 리스트
    List<ViewportMapMarkerDetail> markerDetails = place.stream().map(this::toMarkerDetail).toList();
    // 마커 카테고리 리스트
    List<String> categories =
        place.stream()
            .flatMap(p -> p.getPlaceCategories().stream())
            .map(pc -> pc.getCategory().getName())
            .distinct()
            .toList();

    return ViewportMapMarkerResponse.of(markerDetails, categories);
  }

  private void validViewport(Double swLat, Double swLng, Double neLat, Double neLng) {
    if (swLat > neLat || swLng > neLng) {
      throw new CustomException(ErrorCode.INVALID_VIEWPORT_COORDINATES);
    }
  }

  private void validCategory(String category) {
    if (category != null && !categoryRepository.existsByName(category)) {
      throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
    }
  }

  private ViewportMapMarkerDetail toMarkerDetail(Place p) {
    String firstCategory =
        p.getPlaceCategories().stream()
            .map(pc -> pc.getCategory().getName())
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.UNCATEGORIZED));

    return ViewportMapMarkerDetail.of(p.getId(), p.getLatitude(), p.getLongitude(), firstCategory);
  }
}
