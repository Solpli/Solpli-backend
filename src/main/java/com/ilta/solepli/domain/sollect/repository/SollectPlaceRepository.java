package com.ilta.solepli.domain.sollect.repository;

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
}
