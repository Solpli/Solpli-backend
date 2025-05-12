package com.ilta.solepli.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place.id = :placeId")
  Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);
}
