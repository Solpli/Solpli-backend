package com.ilta.solepli.domain.sollect.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.sollect.dto.request.KeywordRequest;
import com.ilta.solepli.domain.sollect.dto.request.SollectCreateRequest;
import com.ilta.solepli.domain.sollect.dto.request.SollectUpdateRequest;
import com.ilta.solepli.domain.sollect.dto.response.PlaceSearchResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectCreateResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectDetailResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectPlaceAddPreviewResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectSearchResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectSearchResponse.PopularSollectContent;
import com.ilta.solepli.domain.sollect.service.SollectService;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;
import com.ilta.solepli.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sollect")
@Tag(name = "SollectController", description = "쏠렉트 관련 API")
public class SollectController {

  private final SollectService sollectService;

  @Operation(
      summary = "쏠렉트 등록 API",
      description =
          "쏠렉트를 등록하는 API입니다. 제목, 내용, 장소 ID들을 보내주세요. 여기서 내용은 타입 (TEXT or IMAGE), 순서, 내용(TEXT의 경우 글의 내용, IMAGE의 경우 이미지 파일의 이름)입니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<SollectCreateResponse>> createSollect(
      @Valid @RequestBody SollectCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    return ResponseEntity.ok()
        .body(
            SuccessResponse.successWithData(
                sollectService.createSollect(request, userDetails.user())));
  }

  @Operation(summary = "쏠렉트 이미지 업로드 API", description = "쏠렉트의 이미지를 업로드하는 API입니다. (등록 or 수정시 사용)")
  @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<Void>> uploadSollectImage(
      @PathVariable Long id,
      @Parameter(
              description = "업로드할 파일 리스트",
              content = @Content(mediaType = "application/octet-stream"))
          @RequestPart(name = "files", required = false)
          List<MultipartFile> files,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    sollectService.uploadSollectImage(id, files, userDetails.user());

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 이미지 업로드 성공"));
  }

  @Operation(summary = "쏠렉트 상세 조회 API", description = "쏠렉트를 상세 조회하는 API입니다.")
  @GetMapping("/{id}")
  public ResponseEntity<SuccessResponse<SollectDetailResponse>> getSollectDetail(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = null;
    if (userDetails != null) {
      user = userDetails.user();
    }

    SollectDetailResponse sollectDetail = sollectService.getSollectDetail(id, user);

    return ResponseEntity.ok().body(SuccessResponse.successWithData(sollectDetail));
  }

  @Operation(summary = "쏠렉트 수정 API", description = "쏠렉트를 수정하는 API입니다.")
  @PutMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> updateSollect(
      @PathVariable Long id,
      @Valid @RequestBody SollectUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    sollectService.updateSollect(id, request, userDetails.user());
    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 수정 성공"));
  }

  @Operation(summary = "쏠렉트 삭제 API", description = "쏠렉트를 삭제하는 API입니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> deleteSollect(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    sollectService.deleteSollect(id, userDetails.user());
    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 삭제 성공"));
  }

  @Operation(summary = "쏠렉트 최근 검색어 저장 API", description = "쏠렉트에서의 최근 검색어를 저장하는 API 입니다.")
  @PostMapping("/search/recent")
  public ResponseEntity<SuccessResponse<Void>> addRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody KeywordRequest keywordRequest) {

    sollectService.addRecentSearch(customUserDetails.getUsername(), keywordRequest.keyword());

    return ResponseEntity.status(201)
        .body(
            SuccessResponse.successWithNoData(keywordRequest.keyword() + " 검색어가 최근 목록에 반영 되었습니다."));
  }

  @Operation(summary = "쏠렉트 최근 검색어 조회 API", description = "쏠렉트에서의 최근 검색어를 조회하는 API 입니다.")
  @GetMapping("/search/recent")
  public ResponseEntity<SuccessResponse<List<String>>> getRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    List<String> recentSearch = sollectService.getRecentSearch(customUserDetails.getUsername());

    return ResponseEntity.ok(SuccessResponse.successWithData(recentSearch));
  }

  @Operation(summary = "쏠렉트 최근 검색어 삭제 API", description = "쏠렉트에서의 최근 검색어를 삭제하는 API 입니다.")
  @DeleteMapping("/search/recent/{keyword}")
  public ResponseEntity<SuccessResponse<Void>> deleteRecentSearch(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable String keyword) {

    sollectService.deleteRecentSearch(customUserDetails.getUsername(), keyword);

    return ResponseEntity.ok(SuccessResponse.successWithNoData(keyword + " 검색어 삭제 성공"));
  }

  @Operation(summary = "쏠렉트 검색 API", description = "키워드 + 카테고리명으로 쏠렉트를 검색하는 API 입니다.")
  @GetMapping("/search")
  public ResponseEntity<SuccessResponse<SollectSearchResponse>> searchSollect(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category) {

    User user = SecurityUtil.getUser(customUserDetails);

    SollectSearchResponse searchContents =
        sollectService.getSearchContents(user, cursorId, size, keyword, category);

    return ResponseEntity.ok(SuccessResponse.successWithData(searchContents));
  }

  @Operation(summary = "쏠렉트 장소 검색 API", description = "쏠렉트 장소 추가 화면에서 사용되는 검색 API입니다.")
  @GetMapping("/search/place")
  public ResponseEntity<SuccessResponse<List<PlaceSearchResponse>>> searchPlaces(
      @RequestParam(required = true) String keyword) {

    List<PlaceSearchResponse> searchContents = sollectService.getSearchPlaces(keyword);

    return ResponseEntity.ok(SuccessResponse.successWithData(searchContents));
  }

  @Operation(
      summary = "쏠렉트 장소 프리뷰 조회 API",
      description = "쏠렉트 장소 추가 화면에서 사용되는 장소 프리뷰를 조회하는 API입니다.")
  @GetMapping("/search/place/{placeId}")
  public ResponseEntity<SuccessResponse<SollectPlaceAddPreviewResponse>> searchRelatedPlace(
      @PathVariable(required = true) Long placeId) {

    SollectPlaceAddPreviewResponse placePreview = sollectService.getPlacePreview(placeId);

    return ResponseEntity.ok(SuccessResponse.successWithData(placePreview));
  }

  @Operation(summary = "인기 쏠렉트 조회 API", description = "인기(저장 수가 많은) 쏠렉트를 조회하는 API 입니다.")
  @GetMapping("/popular")
  public ResponseEntity<SuccessResponse<List<PopularSollectContent>>> getPopularSollects(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    User user = SecurityUtil.getUser(customUserDetails);

    List<PopularSollectContent> popularSollects = sollectService.getPopularSollects(user);

    return ResponseEntity.ok(SuccessResponse.successWithData(popularSollects));
  }
}
