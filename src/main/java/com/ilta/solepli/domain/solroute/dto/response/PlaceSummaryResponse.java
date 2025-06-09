package com.ilta.solepli.domain.solroute.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record PlaceSummaryResponse(
    String name,
    String detailedCategory,
    Integer recommendationPercent,
    List<String> tags,
    Boolean isMarked,
    Double rating) {}
