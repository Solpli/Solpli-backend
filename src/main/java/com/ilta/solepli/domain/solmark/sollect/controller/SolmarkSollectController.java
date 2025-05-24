package com.ilta.solepli.domain.solmark.sollect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponse;
import com.ilta.solepli.domain.solmark.sollect.service.SolmarkSollectService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solmark/sollect")
@Tag(name = "SolmarkSollectController", description = "쏠마크 - 쏠렉트 관련 API")
public class SolmarkSollectController {

  private final SolmarkSollectService solmarkSollectService;

  @Operation(summary = "쏠마크 - 쏠렉트 추가 API", description = "쏠마크에 쏠렉트를 추가하는 API입니다. 쏠렉트의 id를 보내주세요.")
  @PostMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> createSollect(
      @AuthenticationPrincipal CustomUserDetails userDetails, @NotNull @PathVariable Long id) {

    solmarkSollectService.addSolmarkSollect(userDetails.user(), id);

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠마크에 쏠렉트 추가 성공"));
  }

  @Operation(summary = "쏠마크 - 쏠렉트 조회 API", description = "쏠마크의 쏠렉트를 조회하는 API입니다.")
  @GetMapping
  public ResponseEntity<SuccessResponse<SolmarkSollectResponse>> getSollects(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "6") int size) {

    SolmarkSollectResponse solmarkSollects =
        solmarkSollectService.getSolmarkSollects(userDetails.user(), page, size);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(solmarkSollects));
  }

  @Operation(summary = "쏠마크 - 나의 쏠렉트 조회 API", description = "나의 쏠렉트를 조회하는 API입니다.")
  @GetMapping("/my")
  public ResponseEntity<SuccessResponse<SolmarkSollectResponse>> getMySollects(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "6") int size) {

    SolmarkSollectResponse solmarkSollects =
        solmarkSollectService.getMySollects(userDetails.user(), page, size);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(solmarkSollects));
  }
}
