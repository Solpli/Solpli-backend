package com.ilta.solepli.domain.solmap.dto;

import lombok.Builder;

@Builder
public record MarkerResponse(
    Long id, Double latitude, Double longitude, String category, Boolean isMarked) {}
