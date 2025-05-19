package com.ilta.solepli.domain.solelect.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solelect.dto.request.SolelectCreateRequest;
import com.ilta.solepli.domain.solelect.dto.request.SolelectUpdateRequest;
import com.ilta.solepli.domain.solelect.dto.response.SolelectCreateResponse;
import com.ilta.solepli.domain.solelect.service.SolelectService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solelect")
@Tag(name = "SolelectController", description = "쏠렉트 관련 API")
public class SolelectController {

  private final SolelectService solelectService;

  @Operation(
      summary = "쏠렉트 등록 API",
      description =
          "쏠렉트를 등록하는 API입니다. 제목, 내용, 장소 ID들을 보내주세요. 여기서 내용은 타입 (TEXT or IMAGE), 순서, 내용(TEXT의 경우 글의 내용, IMAGE의 경우 이미지 파일의 이름)입니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<SolelectCreateResponse>> createSolelect(
      @Valid @RequestBody SolelectCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    return ResponseEntity.ok()
        .body(
            SuccessResponse.successWithData(
                solelectService.createSolelect(request, userDetails.user())));
  }

  @Operation(summary = "쏠렉트 이미지 업로드 API", description = "쏠렉트의 이미지를 업로드하는 API입니다. (등록 or 수정시 사용)")
  @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<Void>> uploadSolelectImage(
      @PathVariable Long id,
      @Parameter(
              description = "업로드할 파일 리스트",
              content = @Content(mediaType = "application/octet-stream"))
          @RequestPart(name = "files", required = false)
          List<MultipartFile> files,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    solelectService.uploadSolelectImage(id, files, userDetails.user());

    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 이미지 업로드 성공"));
  }

  @Operation(summary = "쏠렉트 수정 API", description = "쏠렉트를 수정하는 API입니다.")
  @PutMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> updateSolelect(
      @PathVariable Long id,
      @RequestBody SolelectUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    solelectService.updateSolelect(id, request, userDetails.user());
    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 수정 성공"));
  }

  @Operation(summary = "쏠렉트 삭제 API", description = "쏠렉트를 삭제하는 API입니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> deleteSolelect(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    solelectService.deleteSolelect(id, userDetails.user());
    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("쏠렉트 삭제 성공"));
  }
}
