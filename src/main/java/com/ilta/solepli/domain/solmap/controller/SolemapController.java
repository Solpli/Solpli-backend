package com.ilta.solepli.domain.solmap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmap.dto.ViewportMapMarkerResponse;
import com.ilta.solepli.domain.solmap.service.SolemapService;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequestMapping("/api/solemap")
@RequiredArgsConstructor
@Tag(name = "SolemapController", description = "쏠맵 관련 API")
public class SolemapController {

  private final SolemapService solemapService;

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
                solemapService.getMarkersByViewport(swLat, swLng, neLat, neLng, category)));
  }
}
