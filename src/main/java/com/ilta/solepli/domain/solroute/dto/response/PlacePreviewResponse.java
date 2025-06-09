package com.ilta.solepli.domain.solroute.dto.response;

import lombok.Builder;

@Builder
public record PlacePreviewResponse(
    String name, String detailedCategory, String address, Double latitude, Double longitude) {}
