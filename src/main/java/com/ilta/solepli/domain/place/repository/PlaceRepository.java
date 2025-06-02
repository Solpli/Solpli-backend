package com.ilta.solepli.domain.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {

  @Query(
      "SELECT p "
          + "FROM Place p "
          + "JOIN fetch p.placeCategories pc "
          + "JOIN fetch pc.category c "
          + "WHERE p.latitude BETWEEN :swLat AND :neLat "
          + "AND p.longitude BETWEEN :swLng AND :neLng "
          + "AND (:category IS NULL OR c.name = :category)")
  List<Place> findInViewportWithOptionalCategory(
      @Param("swLat") Double swLat,
      @Param("swLng") Double swLng,
      @Param("neLat") Double neLat,
      @Param("neLng") Double neLng,
      @Param("category") String category);

  @Query(
      "SELECT DISTINCT  p "
          + "FROM Place p "
          + "JOIN FETCH p.placeCategories pc "
          + "JOIN FETCH pc.category c "
          + "WHERE p.district = :regionName OR p.neighborhood = :regionName")
  List<Place> findAllByRegionName(@Param("regionName") String regionName);

  Boolean existsByDistrictOrNeighborhood(String district, String neighborhood);

  @Query(
      "SELECT p "
          + "FROM Place p "
          + "JOIN FETCH p.placeCategories pc "
          + "JOIN FETCH pc.category c "
          + "WHERE p.id = :id")
  Optional<Place> findByPlaceId(@Param("id") Long id);
}
