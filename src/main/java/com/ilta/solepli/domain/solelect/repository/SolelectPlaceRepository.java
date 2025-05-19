package com.ilta.solepli.domain.solelect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.solelect.entity.Solelect;
import com.ilta.solepli.domain.solelect.entity.mapping.SolelectPlace;

public interface SolelectPlaceRepository extends JpaRepository<SolelectPlace, Long> {
  @Modifying
  @Query("DELETE FROM SolelectPlace sp WHERE sp.solelect = :solelect")
  void deleteBySolelect(@Param("solelect") Solelect solelect);
}
