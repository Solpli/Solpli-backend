package com.ilta.solepli.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.review.entity.mapping.ReviewTag;
import com.ilta.solepli.domain.solmap.dto.TagInfo;
import com.ilta.solepli.domain.tag.entity.TagType;

@Repository
public interface ReviewTagCustomRepository extends JpaRepository<ReviewTag, Long> {

  @Query(
      """
            SELECT new com.ilta.solepli.domain.solmap.dto.TagInfo(rt.name, COUNT(rt))
            FROM ReviewTag rt
            JOIN rt.review r
            WHERE r.place.id = :placeId AND rt.tagType = :tagType
            GROUP BY rt.name
            ORDER BY COUNT(rt) DESC
            """)
  List<TagInfo> findTagCountsByPlaceAndType(
      @Param("placeId") Long placeId, @Param("tagType") TagType tagType);
}
