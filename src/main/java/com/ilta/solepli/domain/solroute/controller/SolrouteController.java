package com.ilta.solepli.domain.solroute.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solroute.dto.request.SolrouteCreateRequest;
import com.ilta.solepli.domain.solroute.dto.response.PlacePreviewResponse;
import com.ilta.solepli.domain.solroute.dto.response.PlaceSummaryResponse;
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

  @Operation(summary = "추가한 장소 근처 인기 장소 조회 API", description = "추가한 장소 근처 인기 장소 조회 API입니다.")
  @GetMapping("/place/nearby/{placeId}")
  public ResponseEntity<SuccessResponse<List<PlaceSummaryResponse>>> findNearbyPopularPlace(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long placeId) {

    List<PlaceSummaryResponse> nearbyPopularPlace =
        solrouteService.findNearbyPopularPlace(customUserDetails.user(), placeId);

    return ResponseEntity.ok(SuccessResponse.successWithData(nearbyPopularPlace));
  }

  @Operation(
      summary = "쏠루트 코스 생성 - 장소 프리뷰 조회 API",
      description = "쏠루트 코스 생성시 사용하는 장소 프리뷰 조회 API입니다.")
  @GetMapping("/place/{placeId}")
  public ResponseEntity<SuccessResponse<PlacePreviewResponse>> getPlacePreview(
      @PathVariable Long placeId) {

    PlacePreviewResponse placePreview = solrouteService.getPlacePreview(placeId);

    return ResponseEntity.ok(SuccessResponse.successWithData(placePreview));
  }

  @Operation(summary = "쏠루트 코스 상태 변경 API", description = "쏠루트 코스의 상태를 변경할때 사용하는 API입니다.")
  @PatchMapping("/{solrotueId}")
  public ResponseEntity<SuccessResponse<Void>> updateSolrouteStatus(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long solrotueId) {

    String status = solrouteService.updateSolrouteStatus(solrotueId, customUserDetails.user());
    return ResponseEntity.status(200)
        .body(SuccessResponse.successWithNoData("쏠루트 상태 " + status + "(으)로 변경 완료."));
  }
}
