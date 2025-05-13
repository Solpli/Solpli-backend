package com.ilta.solepli.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {}
