package com.ilta.solepli.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record BasicLoginRequest(
    @NotNull(message = "아이디는 필수입니다.") String loginId,
    @NotNull(message = "비밀번호는 필수입니다.") String password) {}
