package com.ilta.solepli.domain.solmap.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record PlaceSearchPreviewResponse(
    List<PlacePreviewDetail> places, Long nextCursor, Double nextCursorDist) {}
