package com.ilta.solepli.domain.sollect.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.sollect.dto.PopularSollectResponseContent;
import com.ilta.solepli.domain.sollect.dto.SollectSearchResponseContent;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponseContent;

@Repository
public interface SollectRepositoryCustom {
  List<SollectSearchResponseContent> searchSollectByKeywordOrCategory(
      Long cursorId, int size, String parsedKeyword, String parsedCategory);

  List<SolmarkSollectResponseContent> searchSolmarkSollectBySollectIdsAndCursor(
      Long cursorId, int size, List<Long> sollectIds);

  List<PopularSollectResponseContent> searchSollectBySollectIds(List<Long> sollectIds);

  List<SollectSearchResponseContent> searchSollectBySollectIdsAndCursor(
      Long cursorId, int size, List<Long> sollectIds);

  List<SollectSearchResponseContent> searchRecommendSollectByKeywordOrCategory(
      String keyword, String categoryName);
}
