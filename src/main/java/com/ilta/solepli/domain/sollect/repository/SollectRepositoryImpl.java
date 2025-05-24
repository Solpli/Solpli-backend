package com.ilta.solepli.domain.sollect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    // 1. 둘 다 null이면 빈 페이지 반환
    if ((parsedKeyword == null || parsedKeyword.isBlank())
        && (parsedCategory == null || parsedCategory.isBlank())) {
      return Page.empty(pageable);
    }

    // 2. 키워드/카테고리에 맞는 Place ID 찾기
    List<Long> placeIds =
        queryFactory
            .select(place.id)
            .from(place)
            .leftJoin(place.placeCategories, placeCategory)
            .leftJoin(placeCategory.category, category)
            .where(anyMatchKeyword(parsedKeyword), matchCategory(parsedCategory))
            .fetch();

    // 3. 안전하게 조합된 keyword 조건
    BooleanBuilder keywordCondition = new BooleanBuilder();
    BooleanExpression titleCondition = matchSollectTitle(parsedKeyword);
    BooleanExpression placeCondition =
        placeIds.isEmpty() ? null : sollectPlace.place.id.in(placeIds);

    if (titleCondition != null) {
      keywordCondition.or(titleCondition);
    }
    if (placeCondition != null) {
      keywordCondition.or(placeCondition);
    }

    // 첫 번째 장소용 Alias 분리
    QSollectPlace firstPlace = new QSollectPlace("firstPlace");
    QPlace firstPlaceInfo = new QPlace("firstPlaceInfo");

    // 4. 본문 쿼리
    List<SollectSearchResponseContent> result =
        queryFactory
            .select(
                new QSollectSearchResponseContent(
                    sollectContent.imageUrl,
                    sollect.title,
                    firstPlaceInfo.district,
                    firstPlaceInfo.neighborhood))
            .from(sollect)
            .join(sollect.sollectPlaces, sollectPlace)
            .join(sollectPlace.place, place)
            .join(sollect.sollectPlaces, firstPlace)
            .on(firstPlace.seq.eq(0))
            .join(firstPlace.place, firstPlaceInfo)
            .join(sollect.sollectContents, sollectContent)
            .where(sollectContent.seq.eq(0L), keywordCondition)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(sollect.createdAt.desc())
            .fetch();

    // 5. 총 개수 카운트
    Long count =
        queryFactory
            .select(sollect.countDistinct())
            .from(sollect)
            .join(sollect.sollectPlaces, sollectPlace)
            .where(keywordCondition)
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
