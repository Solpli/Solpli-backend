package com.ilta.solepli.domain.sollect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.sollect.dto.SollectSearchResponseContent;

@Repository
public interface SollectRepositoryCustom {
  Page<SollectSearchResponseContent> searchSollectByKeywordOrCategory(
      Pageable pageable, String parsedKeyword, String parsedCategory);
}
