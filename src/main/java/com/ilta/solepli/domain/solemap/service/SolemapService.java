package com.ilta.solepli.domain.solemap.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.category.repository.CategoryRepository;
import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.entity.mapping.PlaceCategory;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solemap.dto.ViewportMapMarkerDetail;
import com.ilta.solepli.domain.solemap.dto.ViewportMapMarkerResponse;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolemapService {

  private final PlaceRepository placeRepository;
  private final CategoryRepository categoryRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  private static String RECENT_SEARCH_PREFIX = "recent_search:";
  private static int MAX_RECENT_SEARCH = 10;

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
    List<ViewportMapMarkerDetail> markerDetails =
        place.stream().map(p -> toMarkerDetail(p, category)).toList();
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

  private ViewportMapMarkerDetail toMarkerDetail(Place p, String selectedCategory) {
    String category;

    if (selectedCategory != null) {
      category = selectedCategory;
    } else {
      category =
          p.getPlaceCategories().stream()
              .sorted(Comparator.comparing(PlaceCategory::getId))
              .map(pc -> pc.getCategory().getName())
              .findFirst()
              .orElseThrow(() -> new CustomException(ErrorCode.UNCATEGORIZED));
    }

    return ViewportMapMarkerDetail.builder()
        .id(p.getId())
        .latitude(p.getLatitude())
        .longitude(p.getLongitude())
        .category(category)
        .build();
  }

  /**
   * 사용자가 검색한 키워드를 ZSET에 추가하고, 최대 저장 개수를 초과한 오래된 항목은 삭제한다.
   *
   * @param userId 사용자 식별자
   * @param keyword 검색어
   */
  public void addRecentSearch(String userId, String keyword) {
    String key = keyBuild(userId);
    long score = System.currentTimeMillis();

    // ZSET에 (키워드, timestamp) 쌍으로 추가
    redisTemplate.opsForZSet().add(key, keyword, score);

    // 최신 MAX_RECENT_SEARCH개를 제외한 나머지(가장 오래된) 삭제
    redisTemplate.opsForZSet().removeRange(key, 0, -MAX_RECENT_SEARCH - 1);
  }

  /**
   * Redis에 저장할 키를 생성한다.
   *
   * @param userId 사용자 식별자
   * @return "recent_search:{userId}" 형태의 최종 키
   */
  private String keyBuild(String userId) {
    return RECENT_SEARCH_PREFIX + userId;
  }
}
