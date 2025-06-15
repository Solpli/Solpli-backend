package com.ilta.solepli.domain.solmark.place.dto.reqeust;

import jakarta.validation.constraints.*;

public record UpdateCollectionRequest(
    @Size(min = 1, max = 15, message = "저장 리스트 이름은 1자 이상 15자 이하여야 합니다.") String collectionName,
    @Min(value = 1, message = "iconId는 1 이상이어야 합니다.")
        @Max(value = 14, message = "iconId는 14 이하여야 합니다.")
        Integer iconId) {}
