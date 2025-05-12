package com.ilta.solepli.domain.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.review.dto.request.ReviewCreateRequest;
import com.ilta.solepli.domain.review.service.ReviewService;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@Tag(name = "ReviewController", description = "리뷰 관련 API")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "리뷰 등록 API", description = "장소에 대한 리뷰를 등록하는 API입니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<Void>> createReview(
      @Valid @RequestPart("data") ReviewCreateRequest request,
      @RequestPart("files") List<MultipartFile> files,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    reviewService.createReview(request, files, userDetails.user());
    return ResponseEntity.ok().body(SuccessResponse.successWithNoData("리뷰 등록 성공"));
  }
}
