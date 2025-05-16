package com.ilta.solepli.domain.solemap.dto;

import lombok.Builder;

@Builder
public record ViewportMapMarkerDetail(
    Long id, Double latitude, Double longitude, String category) {}
