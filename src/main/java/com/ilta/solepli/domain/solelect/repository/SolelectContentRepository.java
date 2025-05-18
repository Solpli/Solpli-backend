package com.ilta.solepli.domain.solelect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.solelect.entity.Solelect;
import com.ilta.solepli.domain.solelect.entity.SolelectContent;

public interface SolelectContentRepository extends JpaRepository<SolelectContent, Long> {
  @Modifying
  @Query("DELETE FROM SolelectContent sc WHERE sc.solelect = :solelect")
  void deleteBySolelect(@Param("solelect") Solelect solelect);
}
