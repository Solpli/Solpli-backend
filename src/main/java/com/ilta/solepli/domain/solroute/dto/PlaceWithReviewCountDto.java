package com.ilta.solepli.domain.solroute.dto;

import com.ilta.solepli.domain.place.entity.Place;

public record PlaceWithReviewCountDto(Place place, long reviewCount) {}
