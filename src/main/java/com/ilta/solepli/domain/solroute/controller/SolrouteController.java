package com.ilta.solepli.domain.solroute.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solroute.dto.request.SolrouteCreateRequest;
import com.ilta.solepli.domain.solroute.service.SolrouteService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solroute")
@Tag(name = "SolrouteController", description = "쏠루트 관련 API")
public class SolrouteController {

  private final SolrouteService solrouteService;

  @Operation(summary = "쏠루트 생성 API", description = "쏠루트를 생성하는 API 입니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<Void>> createSolroute(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody SolrouteCreateRequest request) {

    solrouteService.createSolroute(customUserDetails.user(), request);

    return ResponseEntity.status(200).body(SuccessResponse.successWithNoData("쏠루트 생성 완료."));
  }
}
