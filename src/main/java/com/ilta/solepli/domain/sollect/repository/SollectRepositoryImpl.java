package com.ilta.solepli.domain.sollect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
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
  public Page<SollectSearchResponseContent> searchSollectByKeywordOrCategory(
      Pageable pageable, String parsedKeyword, String parsedCategory) {

    // 1. 키워드 또는 카테고리에 해당하는 Place ID 추출
    List<Long> placeIds =
        queryFactory
            .select(place.id)
            .from(place)
            .leftJoin(place.placeCategories, placeCategory)
            .leftJoin(placeCategory.category, category)
            .where(anyMatchKeyword(parsedKeyword), matchCategory(parsedCategory))
            .fetch();

    // 2. Sollect ID 추출 (title OR place 조건 만족)
    BooleanBuilder sollectCondition = new BooleanBuilder();
    BooleanExpression titleCondition = matchSollectTitle(parsedKeyword);
    BooleanExpression placeCondition =
        placeIds.isEmpty() ? null : sollectPlace.place.id.in(placeIds);

    if (titleCondition != null) sollectCondition.or(titleCondition);
    if (placeCondition != null) sollectCondition.or(placeCondition);

    List<Tuple> sollectTuples =
        queryFactory
            .select(sollect.id, sollect.createdAt)
            .from(sollect)
            .leftJoin(sollect.sollectPlaces, sollectPlace)
            .where(sollectCondition)
            .distinct()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(sollect.createdAt.desc())
            .fetch();

    // ID만 추출
    List<Long> sollectIds = sollectTuples.stream().map(tuple -> tuple.get(sollect.id)).toList();

    // 3. DTO 추출 (대표 장소, 대표 콘텐츠)
    QSollectPlace firstPlace = new QSollectPlace("firstPlace");
    QPlace firstPlaceInfo = new QPlace("firstPlaceInfo");

    List<SollectSearchResponseContent> result =
        queryFactory
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
            .orderBy(sollect.createdAt.desc())
            .fetch();

    // 4. 전체 개수 추출
    Long count =
        queryFactory
            .select(sollect.countDistinct())
            .from(sollect)
            .leftJoin(sollect.sollectPlaces, sollectPlace)
            .where(sollectCondition)
            .fetchOne();

    return new PageImpl<>(result, pageable, Optional.ofNullable(count).orElse(0L));
  }

  @Override
  public Page<SolmarkSollectResponseContent> searchBySolmarkSollect(
      Pageable pageable, List<Long> sollectIds) {
    QSollectPlace firstPlace = new QSollectPlace("firstPlace");
    QPlace firstPlaceInfo = new QPlace("firstPlaceInfo");

    List<SolmarkSollectResponseContent> result =
        queryFactory
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
            .where(sollect.id.in(sollectIds))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(sollect.createdAt.desc())
            .fetch();

    // 4. 전체 개수 추출
    Long count =
        queryFactory
            .select(sollect.countDistinct())
            .from(sollect)
            .leftJoin(sollect.sollectPlaces, sollectPlace)
            .where(sollect.id.in(sollectIds))
            .fetchOne();

    return new PageImpl<>(result, pageable, Optional.ofNullable(count).orElse(0L));
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
}
