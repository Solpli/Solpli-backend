package com.ilta.solepli.domain.solroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.solroute.entity.Solroute;
import com.ilta.solepli.domain.solroute.entity.SolroutePlace;

public interface SolroutePlaceRepository extends JpaRepository<SolroutePlace, Long> {
  @Modifying
  @Query("DELETE FROM SolroutePlace sp WHERE sp.solroute = :solroute")
  void deleteAllBySolrouteId(@Param("solroute") Solroute solroute);
}
