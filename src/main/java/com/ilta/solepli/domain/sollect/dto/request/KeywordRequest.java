package com.ilta.solepli.domain.sollect.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KeywordRequest(@NotBlank(message = "검색어를 입력해주세요.") String keyword) {}
