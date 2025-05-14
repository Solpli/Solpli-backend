package com.ilta.solepli.domain.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  @Query(
      "SELECT p "
          + "FROM Place p "
          + "JOIN p.placeCategories pc "
          + "JOIN pc.category c "
          + "WHERE p.latitude BETWEEN :swLat AND :neLat "
          + "AND p.longitude BETWEEN :swLng AND :neLng "
          + "AND (:category IS NULL OR c.name = :category)")
  List<Place> findInViewportWithOptionalCategory(
      @Param("swLat") Double swLat,
      @Param("swLng") Double swLng,
      @Param("neLat") Double neLat,
      @Param("neLng") Double neLng,
      @Param("category") String category);
}
