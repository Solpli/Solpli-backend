package com.ilta.solepli.domain.place.dto.response;

import lombok.Builder;

@Builder
public record PlaceSearchResponse(Long id, String category, String name, String address) {}
