package com.ilta.solepli.domain.solmap.dto;

import lombok.Builder;

import com.ilta.solepli.domain.solmap.entity.SearchType;

@Builder
public record RelatedSearchResponse(
    Long id,
    SearchType type,
    String name,
    String address,
    Distance distance,
    String category,
    Boolean isMarked) {}
