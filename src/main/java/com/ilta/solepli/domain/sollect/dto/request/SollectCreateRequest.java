package com.ilta.solepli.domain.sollect.dto.request;

import java.util.List;

import lombok.Builder;

import com.ilta.solepli.domain.sollect.entity.ContentType;

@Builder
public record SollectCreateRequest(
    String title, List<SollectContent> contents, List<Long> placeIds) {

  public record SollectContent(Long seq, ContentType type, String content) {}
}
