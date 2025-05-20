package com.ilta.solepli.domain.sollect.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import com.ilta.solepli.domain.sollect.entity.ContentType;

@Builder
public record SollectCreateRequest(
    @NotBlank(message = "제목은 필수 입력값입니다.") String title,
    @NotEmpty(message = "내용 목록은 비워둘 수 없습니다.") List<@Valid SollectContent> contents,
    @NotEmpty(message = "장소 목록은 하나 이상 선택해야 합니다.")
        List<@NotNull(message = "장소 ID는 null일 수 없습니다.") Long> placeIds) {
  public record SollectContent(
      @NotNull(message = "내용 순서는 필수입니다.") Long seq,
      @NotNull(message = "콘텐츠 타입은 필수입니다.") ContentType type,
      @NotBlank(message = "본문 내용은 비워둘 수 없습니다.") String content) {}
}
