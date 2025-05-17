package com.ilta.solepli.domain.solelect.dto.request;

import java.util.List;

import lombok.Builder;

import com.ilta.solepli.domain.solelect.entity.ContentType;

@Builder
public record SolelectCreateRequest(
    String title, List<SolelectContent> contents, List<Long> placeIds) {

  public record SolelectContent(Long seq, ContentType type, String content) {}
}
