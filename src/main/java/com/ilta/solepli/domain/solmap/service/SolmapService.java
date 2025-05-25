package com.ilta.solepli.domain.solmap.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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
import com.ilta.solepli.domain.place.entity.PlaceHour;
import com.ilta.solepli.domain.place.entity.QPlace;
import com.ilta.solepli.domain.place.entity.QPlaceHour;
import com.ilta.solepli.domain.place.entity.mapping.PlaceCategory;
import com.ilta.solepli.domain.place.entity.mapping.QPlaceCategory;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.review.entity.QReview;
import com.ilta.solepli.domain.review.entity.Review;
import com.ilta.solepli.domain.review.entity.mapping.QReviewImage;
import com.ilta.solepli.domain.review.entity.mapping.QReviewTag;
import com.ilta.solepli.domain.review.entity.mapping.ReviewImage;
import com.ilta.solepli.domain.solmap.dto.*;
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
  private final QReview r = QReview.review;
  private final QReviewTag rt = QReviewTag.reviewTag;
  private final QReviewImage ri = QReviewImage.reviewImage;
  private final QPlaceHour ph = QPlaceHour.placeHour;

  private static final String RECENT_SEARCH_PREFIX = "solmap_recent_search:";
  private static final int MAX_RECENT_SEARCH = 10;
  private static final int TAG_LIMIT = 3;
  private static final int THUMBNAIL_LIMIT = 3;

  @Transactional(readOnly = true)
  public List<ViewportMapMarkerDetail> getMarkersByViewport(
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
                  List<String> topTagsForPlace = getTopTagsForPlace(p.getId(), TAG_LIMIT);
                  List<String> reviewThumbnails = getReviewThumbnails(p.getId(), THUMBNAIL_LIMIT);
                  OpenStatus openStatus = getOpenStatus(p);
                  Integer isSoloRecommendedPercent = getRecommendationPercent(p.getId());

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

  // 장소별 최다 리뷰 태그 n개 조회
  private List<String> getTopTagsForPlace(Long placeId, int limit) {
    return jpaQueryFactory
        .select(rt.name)
        .from(rt)
        .join(rt.review, r)
        .where(rt.review.place.id.eq(placeId))
        .groupBy(rt.name)
        .orderBy(rt.id.count().desc(), rt.name.asc())
        .limit(limit)
        .fetch();
  }

  // 장소별 최신 리뷰의 썸네일 이미지 n개 조회
  private List<String> getReviewThumbnails(Long placeId, int limit) {
    List<Review> reviews =
        jpaQueryFactory
            .selectFrom(r)
            .distinct()
            .join(r.reviewImages, ri)
            .where(r.place.id.eq(placeId))
            .orderBy(r.createdAt.desc())
            .limit(limit)
            .fetch();

    return reviews.stream()
        .map(r -> r.getReviewImages().stream().findFirst().map(ReviewImage::getImageUrl))
        .map(Optional::toString)
        .toList();
  }

  // 현재 영업중 여부 및 마감 시간 반환
  private OpenStatus getOpenStatus(Place place) {
    int todayNow = LocalDate.now().getDayOfWeek().getValue() % 7;

    LocalTime now = LocalTime.now();

    Optional<LocalTime> endTime =
        place.getPlaceHours().stream()
            .filter(ph -> ph.getDayOfWeek() == todayNow)
            .filter(ph -> !now.isBefore(ph.getStartTime()) && !now.isAfter(ph.getEndTime()))
            .map(PlaceHour::getEndTime)
            .findFirst();

    return OpenStatus.of(endTime.isPresent(), endTime.orElse(null));
  }

  // 장소별 추천 비율 반환 (0~100)
  private Integer getRecommendationPercent(Long placeId) {
    Long recommendedCountObj =
        jpaQueryFactory
            .select(r.count())
            .from(r)
            .where(r.place.id.eq(placeId).and(r.recommendation.eq(true)))
            .fetchOne();

    Long totalCountObj =
        jpaQueryFactory.select(r.count()).from(r).where(r.place.id.eq(placeId)).fetchOne();

    long recommendedCount = (recommendedCountObj == null) ? 0L : recommendedCountObj;
    long totalCount = (totalCountObj == null) ? 0L : totalCountObj;

    if (totalCount == 0) {
      return null;
    }

    double percent = recommendedCount * 100.0 / totalCount;
    return (int) percent;
  }
}
