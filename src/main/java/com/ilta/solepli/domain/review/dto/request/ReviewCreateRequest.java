package com.ilta.solepli.domain.review.dto.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
    @NotNull(message = "장소 ID는 필수입니다.") Long placeId,
    @NotNull(message = "1인 추천 여부는 필수입니다.") Boolean recommendation,
    @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5 이하여야 합니다.")
        Integer rating,
    @NotEmpty(message = "분위기 태그는 최소 1개 이상 선택해야 합니다.")
        List<@NotBlank(message = "분위기 태그 값은 비어 있을 수 없습니다.") String> moodTag,
    @NotEmpty(message = "1인 이용 태그는 최소 1개 이상 선택해야 합니다.")
        List<@NotBlank(message = "1인 이용 태그 값은 비어 있을 수 없습니다.") String> soloTag,
    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력할 수 있습니다.") String content) {}
