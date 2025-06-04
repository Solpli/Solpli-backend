package com.ilta.solepli.domain.sollect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.entity.mapping.SollectPlace;

public interface SollectPlaceRepository extends JpaRepository<SollectPlace, Long> {
  @Modifying
  @Query("DELETE FROM SollectPlace sp WHERE sp.sollect = :sollect")
  void deleteBySollect(@Param("sollect") Sollect sollect);

  @Query(
      """
      SELECT sp FROM SollectPlace sp
      JOIN FETCH sp.place
      WHERE sp.sollect.id = :sollectId
      ORDER BY sp.seq ASC
      """)
  List<SollectPlace> findBySollectIdWithPlace(@Param("sollectId") Long sollectId);

  @Query("SELECT sp.sollect.id FROM SollectPlace sp WHERE sp.place.id = :placeId")
  List<Long> findSollectIdsByPlaceId(@Param("placeId") Long placeId);
}
