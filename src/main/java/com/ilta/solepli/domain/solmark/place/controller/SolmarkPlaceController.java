package com.ilta.solepli.domain.solmark.place.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmark.place.dto.reqeust.*;
import com.ilta.solepli.domain.solmark.place.dto.response.CollectionResponse;
import com.ilta.solepli.domain.solmark.place.dto.response.SolmarkPlacesResponse;
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

  @Operation(summary = "쏠마크 장소 저장 리스트 조회 API", description = "쏠마크 장소 저장 리스트 조회 API 입니다.")
  @GetMapping("collections")
  public ResponseEntity<SuccessResponse<List<CollectionResponse>>> getCollections(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    List<CollectionResponse> response = solmarkPlaceService.getCollections(customUserDetails);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(response));
  }

  @Operation(summary = "쏠마크 장소 리스트 조회 API", description = "쏠마크 장소 리스트 조회 API 입니다.")
  @GetMapping("/collections/{collectionId}/places")
  public ResponseEntity<SuccessResponse<SolmarkPlacesResponse>> getSolmarkPlaces(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long collectionId) {

    SolmarkPlacesResponse response =
        solmarkPlaceService.getSolmarkPlaces(customUserDetails, collectionId);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(response));
  }

  @Operation(summary = "쏠마크 장소 저장 리스트 수정 API", description = "쏠마크 장소 저장 리스트 수정 API 입니다.")
  @PatchMapping("/collections/{collectionId}")
  public ResponseEntity<SuccessResponse<Void>> updateCollection(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long collectionId,
      @Valid @RequestBody UpdateCollectionRequest updateCollectionRequest) {

    solmarkPlaceService.updateCollection(customUserDetails, collectionId, updateCollectionRequest);

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠마크 저장 리스트 수정 성공"));
  }

  @Operation(summary = "쏠마크 장소 저장 리스트 삭제 API", description = "쏠마크 장소 저장 리스트 삭제 API 입니다.")
  @DeleteMapping("/collections/{collectionId}")
  public ResponseEntity<SuccessResponse<Void>> deleteCollection(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long collectionId) {

    solmarkPlaceService.deleteCollection(customUserDetails, collectionId);

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠마크 저장 리스트 삭제 성공"));
  }

  @Operation(summary = "쏠마크 장소 추가 및 삭제 API", description = "쏠마크 장소 추가 및 삭제 API 입니다.")
  @PatchMapping("/{placeId}/collections")
  public ResponseEntity<SuccessResponse<Void>> updatePlaceCollections(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long placeId,
      @RequestBody UpdatePlaceCollectionsRequest updatePlaceCollectionsRequest) {

    solmarkPlaceService.updatePlaceCollections(
        customUserDetails, placeId, updatePlaceCollectionsRequest);

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠마크 장소 업데이트 성공"));
  }
}
