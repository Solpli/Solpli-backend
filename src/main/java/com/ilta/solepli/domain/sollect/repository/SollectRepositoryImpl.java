package com.ilta.solepli.domain.sollect.repository;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.category.entity.QCategory;
import com.ilta.solepli.domain.place.entity.QPlace;
import com.ilta.solepli.domain.place.entity.mapping.QPlaceCategory;
import com.ilta.solepli.domain.sollect.dto.QSollectSearchResponseContent;
import com.ilta.solepli.domain.sollect.dto.SollectSearchResponseContent;
import com.ilta.solepli.domain.sollect.entity.QSollect;
import com.ilta.solepli.domain.sollect.entity.QSollectContent;
import com.ilta.solepli.domain.sollect.entity.mapping.QSollectPlace;
import com.ilta.solepli.domain.solmark.sollect.dto.response.QSolmarkSollectResponseContent;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponseContent;

@RequiredArgsConstructor
public class SollectRepositoryImpl implements SollectRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QSollect sollect = QSollect.sollect;
  QSollectPlace sollectPlace = QSollectPlace.sollectPlace;
  QSollectContent sollectContent = QSollectContent.sollectContent;
  QPlace place = QPlace.place;
  QPlaceCategory placeCategory = QPlaceCategory.placeCategory;
  QCategory category = QCategory.category;

  @Override
  public List<SollectSearchResponseContent> searchSollectByKeywordOrCategory(
      Long cursorId, int size, String parsedKeyword, String parsedCategory) {

    // 1. 키워드 또는 카테고리에 해당하는 Place ID 추출
    List<Long> placeIds =
        queryFactory
            .select(place.id)
            .from(place)
            .leftJoin(place.placeCategories, placeCategory)
            .leftJoin(placeCategory.category, category)
            .where(anyMatchKeyword(parsedKeyword), matchCategory(parsedCategory))
            .fetch();

    BooleanBuilder sollectCondition = new BooleanBuilder();
    BooleanExpression titleCond = matchSollectTitle(parsedKeyword);
    BooleanExpression placeCond = placeIds.isEmpty() ? null : sollectPlace.place.id.in(placeIds);
    if (titleCond != null) sollectCondition.or(titleCond);
    if (placeCond != null) sollectCondition.or(placeCond);

    // 커서 조건 추가
    BooleanExpression cursorCondition = cursorLessThan(cursorId);
    if (cursorCondition != null) sollectCondition.and(cursorCondition);

    // ID 추출
    List<Long> sollectIds =
        queryFactory
            .select(sollect.id)
            .from(sollect)
            .leftJoin(sollect.sollectPlaces, sollectPlace)
            .where(sollectCondition)
            .distinct()
            .orderBy(sollect.id.desc())
            .limit(size + 1)
            .fetch();

    // DTO 반환
    QSollectPlace firstPlace = new QSollectPlace("firstPlace");
    QPlace firstPlaceInfo = new QPlace("firstPlaceInfo");

    return queryFactory
        .select(
            new QSollectSearchResponseContent(
                sollect.id,
                sollectContent.imageUrl,
                sollect.title,
                firstPlaceInfo.district,
                firstPlaceInfo.neighborhood))
        .from(sollect)
        .join(sollect.sollectPlaces, firstPlace)
        .on(firstPlace.seq.eq(0))
        .join(firstPlace.place, firstPlaceInfo)
        .join(sollect.sollectContents, sollectContent)
        .on(sollectContent.seq.eq(0L))
        .where(sollect.id.in(sollectIds))
        .orderBy(sollect.id.desc())
        .fetch();
  }

  @Override
  public List<SolmarkSollectResponseContent> searchBySolmarkSollect(
      Long cursorId, int size, List<Long> sollectIds) {
    QSollectPlace firstPlace = new QSollectPlace("firstPlace");
    QPlace firstPlaceInfo = new QPlace("firstPlaceInfo");

    // 커서 조건 추가
    BooleanExpression cursorCondition = cursorLessThan(cursorId);

    return queryFactory
        .select(
            new QSolmarkSollectResponseContent(
                sollect.id,
                sollectContent.imageUrl,
                sollect.title,
                firstPlaceInfo.district,
                firstPlaceInfo.neighborhood))
        .from(sollect)
        .join(sollect.sollectPlaces, firstPlace)
        .on(firstPlace.seq.eq(0))
        .join(firstPlace.place, firstPlaceInfo)
        .join(sollect.sollectContents, sollectContent)
        .on(sollectContent.seq.eq(0L))
        .where(sollect.id.in(sollectIds), cursorCondition)
        .orderBy(sollect.id.desc())
        .limit(size + 1)
        .fetch();
  }

  private BooleanExpression anyMatchKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) return null;
    return place
        .address
        .containsIgnoreCase(keyword)
        .or(place.district.containsIgnoreCase(keyword))
        .or(place.neighborhood.containsIgnoreCase(keyword));
  }

  private BooleanExpression matchCategory(String categoryName) {
    if (categoryName == null || categoryName.isBlank()) return null;
    return category.name.eq(categoryName);
  }

  private BooleanExpression matchSollectTitle(String keyword) {
    if (keyword == null || keyword.isBlank()) return null;
    return sollect.title.containsIgnoreCase(keyword);
  }

  private BooleanExpression cursorLessThan(Long cursorId) {
    return (cursorId != null) ? sollect.id.lt(cursorId) : null;
  }
}
