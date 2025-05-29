package com.ilta.solepli.domain.sollect.dto.response;

import lombok.Builder;

@Builder
public record SollectPlaceAddPreviewResponse(
    Long id, String category, String name, String address) {}
