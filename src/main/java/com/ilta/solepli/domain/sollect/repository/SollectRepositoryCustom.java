package com.ilta.solepli.domain.sollect.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.sollect.dto.SollectSearchResponseContent;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponseContent;

@Repository
public interface SollectRepositoryCustom {
  List<SollectSearchResponseContent> searchSollectByKeywordOrCategory(
      Long cursorId, int size, String parsedKeyword, String parsedCategory);

  Page<SolmarkSollectResponseContent> searchBySolmarkSollect(
      Pageable pageable, List<Long> sollectIds);
}
