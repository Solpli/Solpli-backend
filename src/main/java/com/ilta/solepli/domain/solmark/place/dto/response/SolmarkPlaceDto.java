package com.ilta.solepli.domain.solmark.place.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record SolmarkPlaceDto(
    Long PlaceId,
    String name,
    String detailedCategory,
    Integer recommendationPercent,
    List<String> tags,
    Double rating) {}
