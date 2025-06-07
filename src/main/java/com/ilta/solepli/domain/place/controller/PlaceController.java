package com.ilta.solepli.domain.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.dto.response.PlaceSearchResponse;
import com.ilta.solepli.domain.place.service.PlaceService;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place")
@Tag(name = "PlaceController", description = "장소 관련 API")
public class PlaceController {

  private final PlaceService placeService;

  @Operation(summary = "장소 검색 API", description = "장소 추가 화면에서 사용되는 검색 API입니다.")
  @GetMapping("/search")
  public ResponseEntity<SuccessResponse<List<PlaceSearchResponse>>> searchPlaces(
      @RequestParam(required = true) String keyword) {

    List<PlaceSearchResponse> searchContents = placeService.getSearchPlaces(keyword);

    return ResponseEntity.ok(SuccessResponse.successWithData(searchContents));
  }
}
