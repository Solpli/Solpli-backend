package com.ilta.solepli.domain.solmap.service;

import static com.ilta.solepli.global.util.OpenStatusUtil.getOpenStatus;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ilta.solepli.domain.category.entity.QCategory;
import com.ilta.solepli.domain.category.repository.CategoryRepository;
import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.entity.QPlace;
import com.ilta.solepli.domain.place.entity.QPlaceHour;
import com.ilta.solepli.domain.place.entity.mapping.PlaceCategory;
import com.ilta.solepli.domain.place.entity.mapping.QPlaceCategory;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solmap.dto.*;
import com.ilta.solepli.domain.solmap.entity.SearchType;
import com.ilta.solepli.global.dto.OpenStatus;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolmapService {

  private final PlaceRepository placeRepository;
  private final CategoryRepository categoryRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final JPAQueryFactory jpaQueryFactory;
  private final QPlace p = QPlace.place;
  private final QPlaceCategory pc = QPlaceCategory.placeCategory;
  private final QCategory c = QCategory.category;
  private final QPlaceHour ph = QPlaceHour.placeHour;

  private static final String RECENT_SEARCH_PREFIX = "solmap_recent_search:";
  private static final int MAX_RECENT_SEARCH = 10;
  private static final int TAG_LIMIT = 3;
  private static final int THUMBNAIL_LIMIT = 3;
  private static final int MAX_RELATED_SEARCH = 10;

  @Transactional(readOnly = true)
  public List<MarkerResponse> getMarkersByViewport(
      Double swLat, Double swLng, Double neLat, Double neLng, String category) {

    // 좌표, 카테고리 유효성 검증
    validViewport(swLat, swLng, neLat, neLng);
    validCategory(category);

    // 좌표에 속한 장소 조회
    List<Place> place =
        placeRepository.findInViewportWithOptionalCategory(swLat, swLng, neLat, neLng, category);

    // 마커 관련 데이터 리스트
    return place.stream().map(p1 -> toMarkerDetail(p1, category)).toList();
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

  private MarkerResponse toMarkerDetail(Place p, String selectedCategory) {
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

    return MarkerResponse.builder()
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

  /**
   * 사용자의 최근 검색어를 최신순으로 조회한다.
   *
   * @param userId 사용자 식별자
   * @return 최대 MAX_RECENT_SEARCH개까지의 검색어 리스트
   */
  public List<String> getRecentSearch(String userId) {
    String key = keyBuild(userId);

    // ZSET을 score 내림차순으로 조회
    Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, 0, MAX_RECENT_SEARCH - 1);

    if (range == null || range.isEmpty()) {
      return Collections.emptyList();
    }

    // Object → String 변환
    return range.stream().map(Object::toString).toList();
  }

  /**
   * 사용자의 특정 검색어를 ZSET에서 제거한다. 존재하지 않는 키워드면 404 예외를 던진다.
   *
   * @param userId 사용자 식별자
   * @param keyword 삭제할 검색어
   */
  public void deleteRecentSearch(String userId, String keyword) {
    String key = keyBuild(userId);

    Long removed = redisTemplate.opsForZSet().remove(key, keyword);

    // 삭제결과가 없거나 null 이면 예외 발생
    if (removed == null || removed == 0) {
      throw new CustomException(ErrorCode.RECENT_SEARCH_NOT_FOUND);
    }
  }

  @Transactional
  public PlaceSearchPreviewResponse getPlacesPreview(
      Double swLat,
      Double swLng,
      Double neLat,
      Double neLng,
      Double userLat,
      Double userLng,
      String category,
      Long cursorId,
      Double cursorDist,
      int limit) {

    // 좌표, 카테고리 유효성 검증
    validViewport(swLat, swLng, neLat, neLng);
    validCategory(category);

    NumberExpression<Double> distance = distance(userLat, userLng);

    // 좌표에 속한 장소 조회
    List<Place> places =
        jpaQueryFactory
            .selectFrom(p)
            .distinct()
            .leftJoin(p.placeCategories, pc)
            .leftJoin(pc.category, c)
            .leftJoin(p.placeHours, ph)
            .where(
                viewPortIn(swLat, swLng, neLat, neLng),
                categoryIn(category),
                cursorAfter(cursorId, cursorDist, distance))
            .orderBy(distance.asc(), p.id.asc())
            .limit(limit + 1) // 커서 페이징을 위해 limit+1개 조회 (limit개 + nextCursor용 1개)
            .fetch();

    // 다음 페이지 커서 값 세팅 (limit+1번째 데이터가 존재할 경우에만)
    Long nextCursor = null;
    Double nextCursorDist = null;
    if (places.size() > limit) {
      nextCursor = places.get(limit - 1).getId();
      Place place = places.get(limit - 1);
      nextCursorDist =
          getNextCursorDistance(userLat, userLng, place.getLatitude(), place.getLongitude());
    }

    List<PlacePreviewDetail> placePreviewDetails =
        places.stream()
            .limit(limit) // limit개만 결과로 반환
            .map(
                p -> {
                  List<String> topTagsForPlace =
                      placeRepository.getTopTagsForPlace(p.getId(), TAG_LIMIT);
                  List<String> reviewThumbnails =
                      placeRepository.getReviewThumbnails(p.getId(), THUMBNAIL_LIMIT);
                  OpenStatus openStatus = getOpenStatus(p);
                  Integer isSoloRecommendedPercent =
                      placeRepository.getRecommendationPercent(p.getId());

                  return PlacePreviewDetail.builder()
                      .id(p.getId())
                      .name(p.getName())
                      .detailedCategory(p.getTypes())
                      .tags(topTagsForPlace)
                      .isSoloRecommended(isSoloRecommendedPercent)
                      .rating(p.getRating())
                      .isOpen(openStatus.isOpen())
                      .closingTime(openStatus.closingTime())
                      .thumbnailUrls(reviewThumbnails)
                      .build();
                })
            .toList();

    return PlaceSearchPreviewResponse.builder()
        .places(placePreviewDetails)
        .nextCursor(nextCursor)
        .nextCursorDist(nextCursorDist)
        .build();
  }

  // 지도 뷰포트 내 포함 여부 조건
  private BooleanExpression viewPortIn(Double swLat, Double swLng, Double neLat, Double neLng) {
    return p.latitude.between(swLat, neLat).and(p.longitude.between(swLng, neLng));
  }

  private BooleanExpression categoryIn(String category) {
    if (category == null) {
      return null;
    }
    return c.name.eq(category);
  }

  // 장소 ~ 사용자 거리 계산(Haversine 공식, km단위)
  private NumberExpression<Double> distance(double userLat, double userLng) {
    return Expressions.numberTemplate(
        Double.class,
        "6371 * acos("
            + " cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) +"
            + " sin(radians({0})) * sin(radians({1}))"
            + ")",
        userLat, // {0}
        p.latitude, // {1}
        p.longitude, // {2}
        userLng // {3}
        );
  }

  // 커서(다음 페이지)용 거리 계산
  private Double getNextCursorDistance(
      Double userLat, Double userLng, Double placeLat, Double PlaceLng) {
    double radUserLat = Math.toRadians(userLat);
    double radUserLng = Math.toRadians(userLng);
    double radPlaceLat = Math.toRadians(placeLat);
    double radPlaceLng = Math.toRadians(PlaceLng);

    return 6371
        * Math.acos(
            Math.sin(radUserLat) * Math.sin(radPlaceLat)
                + Math.cos(radUserLat)
                    * Math.cos(radPlaceLat)
                    * Math.cos(radUserLng - radPlaceLng));
  }

  // 커서 이후 데이터 조건 (거리, id순)
  private BooleanExpression cursorAfter(
      Long cursorId, Double cursorDist, NumberExpression<Double> distance) {
    if (cursorId == null || cursorDist == null) {
      return null;
    }

    return distance.gt(cursorDist).or(distance.eq(cursorDist).and(p.id.gt(cursorId)));
  }

  @Transactional(readOnly = true)
  public List<RelatedSearchResponse> getRelatedSearch(
      String keyword, Double userLat, Double userLng) {
    // 구 검색 결과 스트림
    Stream<RelatedSearchResponse> placesDistrictLike = getDistrictsByKeyword(keyword);
    // 동 검색 결과 스트림
    Stream<RelatedSearchResponse> placesNeighborhoodLike = getNeighborhoodsByKeyword(keyword);
    // 장소 검색 결과 스트림 (거리순)
    Stream<RelatedSearchResponse> placesNameLike = getPlacesByKeyword(keyword, userLat, userLng);

    // 스트림을 합쳐서, 앞에서부터 MAX개만 리스트로 수집
    return Stream.of(placesDistrictLike, placesNeighborhoodLike, placesNameLike)
        .flatMap(Function.identity())
        .limit(MAX_RELATED_SEARCH)
        .toList();
  }

  /** keyword가 포함된 구 명을 중복 없이 조회하여 DTO로 매핑한 스트림을 반환. */
  private Stream<RelatedSearchResponse> getDistrictsByKeyword(String keyword) {
    return jpaQueryFactory
        .select(p.district)
        .distinct()
        .from(p)
        .where(p.district.contains(keyword))
        .fetch()
        .stream()
        .map(s -> RelatedSearchResponse.builder().type(SearchType.DISTRICT).name(s).build());
  }

  /** keyword가 포함된 동 명을 중복 없이 조회하여 DTO로 매핑한 스트림을 반환. */
  private Stream<RelatedSearchResponse> getNeighborhoodsByKeyword(String keyword) {
    return jpaQueryFactory
        .select(p.neighborhood)
        .distinct()
        .from(p)
        .where(p.neighborhood.contains(keyword))
        .fetch()
        .stream()
        .map(s -> RelatedSearchResponse.builder().type(SearchType.DISTRICT).name(s).build());
  }

  /** keyword가 포함된 장소를 거리순으로 조회하여 DTO로 매핑한 스트림을 반환. */
  private Stream<RelatedSearchResponse> getPlacesByKeyword(
      String keyword, Double userLat, Double userLng) {
    return jpaQueryFactory
        .select(p)
        .from(p)
        .join(p.placeCategories, pc)
        .join(pc.category, c)
        .where(p.name.contains(keyword))
        .orderBy(distance(userLat, userLng).asc())
        .fetch()
        .stream()
        .map(
            p ->
                RelatedSearchResponse.builder()
                    .id(p.getId())
                    .type(SearchType.PLACE)
                    .name(p.getName())
                    .address(p.getAddress())
                    // 미터 단위 계산 후, m/km DTO로 변환
                    .distance(
                        Distance.fromMeter(
                            (int)
                                calculateDistance(
                                    userLat, userLng, p.getLatitude(), p.getLongitude())))
                    .category(getMainCategory(p))
                    .build());
  }

  /** 장소에 연결된 카테고리 중 첫 번째(대표) 카테고리명을 반환. */
  private String getMainCategory(Place place) {
    return place.getPlaceCategories().get(0).getCategory().getName();
  }

  /** 두 위경도 좌표 간의 거리를 계산하여 미터 단위(double)로 반환. */
  private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
    final int EARTH_RADIUS = 6371000; // 지구 반지름 (미터 단위)

    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c; // 결과: 미터(m) 단위 거리
  }

  public List<MarkerResponse> getMarkersByRegion(String regionName) {
    // regionName과 동일한 구, 동 장소 조회
    return getMarkerByRegion(regionName);
  }

  private List<MarkerResponse> getMarkerByRegion(String regionName) {
    // 조회된 각 장소를 DTO 변환
    return placeRepository.findAllByRegionName(regionName).stream()
        .map(this::getMarkerResponse)
        .toList();
  }

  private MarkerResponse getMarkerResponse(Place p) {
    // 응답 DTO 변환
    return MarkerResponse.builder()
        .id(p.getId())
        .latitude(p.getLatitude())
        .longitude(p.getLongitude())
        .category(getMainCategory(p))
        .build();
  }
}
