package com.ilta.solepli.domain.solemap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KeywordRequest {
  @NotBlank(message = "검색어를 입력해주세요.")
  private String keyword;
}
