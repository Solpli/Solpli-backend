package com.ilta.solepli.domain.solmap.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmap.dto.KeywordRequest;
import com.ilta.solepli.domain.solmap.dto.PlaceSearchPreviewResponse;
import com.ilta.solepli.domain.solmap.dto.ViewportMapMarkerResponse;
import com.ilta.solepli.domain.solmap.service.SolmapService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequestMapping("/api/solmap")
@RequiredArgsConstructor
@Tag(name = "SolmapController", description = "쏠맵 관련 API")
public class SolmapController {

  private final SolmapService solmapService;

  @Operation(summary = "지도 화면 내 장소 마커 정보 조회 API", description = "지도 화면 내 장소 마커 정보들을 조회하는 API 입니다.")
  @GetMapping("/markers")
  public ResponseEntity<SuccessResponse<ViewportMapMarkerResponse>> getMarkersByViewport(
      @RequestParam Double swLat,
      @RequestParam Double swLng,
      @RequestParam Double neLat,
      @RequestParam Double neLng,
      @RequestParam(required = false) String category) {

    return ResponseEntity.ok()
        .body(
            SuccessResponse.successWithData(
                solmapService.getMarkersByViewport(swLat, swLng, neLat, neLng, category)));
  }

  @Operation(summary = "최근 검색어 저장 API", description = "최근 검색어를 저장하는 API 입니다.")
  @PostMapping("/search/recent")
  public ResponseEntity<SuccessResponse<Void>> addRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody KeywordRequest keywordRequest) {

    solmapService.addRecentSearch(customUserDetails.getUsername(), keywordRequest.getKeyword());

    return ResponseEntity.status(201)
        .body(
            SuccessResponse.successWithNoData(
                keywordRequest.getKeyword() + " 검색어가 최근 목록에 반영 되었습니다."));
  }

  @Operation(summary = "최근 검색어 조회 API", description = "최근 검색어를 조회하는 API 입니다.")
  @GetMapping("/search/recent")
  public ResponseEntity<SuccessResponse<List<String>>> getRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    List<String> recentSearch = solmapService.getRecentSearch(customUserDetails.getUsername());

    return ResponseEntity.ok(SuccessResponse.successWithData(recentSearch));
  }

  @Operation(summary = "최근 검색어 삭제 API", description = "최근 검색어를 삭제하는 API 입니다.")
  @DeleteMapping("/search/recent/{keyword}")
  public ResponseEntity<SuccessResponse<Void>> deleteRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable String keyword) {

    solmapService.deleteRecentSearch(customUserDetails.getUsername(), keyword);

    return ResponseEntity.ok(SuccessResponse.successWithNoData(keyword + " 검색어 삭제 성공"));
  }

  @Operation(
      summary = "지도 화면 내 선택된 카테고리별 장소 리스트 조회 API",
      description = "지도 화면 내 선택된 카테고리별 장소 리스트 조회 API 입니다.")
  @GetMapping("/places")
  public ResponseEntity<SuccessResponse<PlaceSearchPreviewResponse>> getPlacesPreview(
      @RequestParam Double swLat,
      @RequestParam Double swLng,
      @RequestParam Double neLat,
      @RequestParam Double neLng,
      @RequestParam Double userLat,
      @RequestParam Double userLng,
      @RequestParam String category,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(required = false) Double cursorDist,
      @RequestParam(required = false, defaultValue = "5") int limit) {

    PlaceSearchPreviewResponse response =
        solmapService.getPlacesPreview(
            swLat, swLng, neLat, neLng, userLat, userLng, category, cursorId, cursorDist, limit);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(response));
  }
}
