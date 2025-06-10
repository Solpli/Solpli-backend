package com.ilta.solepli.domain.solmark.place.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmark.place.dto.reqeust.AddSolmarkPlaceRequest;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.CreateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.service.SolmarkPlaceService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequestMapping("/api/solmark/place")
@RequiredArgsConstructor
@Tag(name = "SolmarkPlaceController", description = "쏠마크 장소 관련 API")
public class SolmarkPlaceController {

  private final SolmarkPlaceService solmarkPlaceService;

  @Operation(summary = "쏠마크 장소 저장 리스트 추가 API", description = "쏠마크 장소 저장 리스트 추가 API 입니다.")
  @PostMapping("/collection")
  public ResponseEntity<SuccessResponse<Void>> createCollection(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody CreateCollectionRequest createCollectionRequest) {

    solmarkPlaceService.createCollection(customUserDetails, createCollectionRequest);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.successWithNoData("쏠마크 저장 리스트 추가 성공"));
  }

  @Operation(summary = "쏠마크 장소 추가 API", description = "쏠마크 장소 저장 리스트 추가 API 입니다.")
  @PostMapping("")
  public ResponseEntity<SuccessResponse<Void>> addSolmarkPlace(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody AddSolmarkPlaceRequest addSolmarkPlaceRequest) {

    solmarkPlaceService.addSolmarkPlace(customUserDetails, addSolmarkPlaceRequest);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.successWithNoData("쏠마크 장소 추가 성공"));
  }
}
