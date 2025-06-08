package com.ilta.solepli.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.review.dto.request.ReviewCreateRequest;
import com.ilta.solepli.domain.review.entity.Review;
import com.ilta.solepli.domain.review.entity.mapping.ReviewImage;
import com.ilta.solepli.domain.review.entity.mapping.ReviewTag;
import com.ilta.solepli.domain.review.repository.ReviewRepository;
import com.ilta.solepli.domain.tag.entity.MoodTag;
import com.ilta.solepli.domain.tag.entity.SoloTag;
import com.ilta.solepli.domain.tag.entity.TagType;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;
import com.ilta.solepli.global.service.S3Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final PlaceRepository placeRepository;
  private final ReviewRepository reviewRepository;
  private final S3Service s3Service;

  @Transactional
  public void createReview(ReviewCreateRequest request, List<MultipartFile> files, User user) {
    Place place =
        placeRepository
            .findById(request.placeId())
            .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));

    if (reviewRepository.existsByUserAndPlace(user, place)) {
      throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    // 리뷰 저장
    Review review =
        Review.builder()
            .recommendation(request.recommendation())
            .rating(request.rating())
            .content(request.content())
            .place(place)
            .user(user)
            .build();

    // 리뷰 태그 저장
    List<ReviewTag> reviewTags = new ArrayList<>();

    // 분위기 태그
    for (String tag : request.moodTag()) {
      if (!MoodTag.isValid(tag)) {
        throw new CustomException(ErrorCode.TAG_NOT_EXISTS);
      }
      reviewTags.add(ReviewTag.builder().name(tag).review(review).tagType(TagType.MOOD).build());
    }

    // 1인 이용 태그
    for (String tag : request.soloTag()) {
      if (!SoloTag.isValid(tag)) {
        throw new CustomException(ErrorCode.TAG_NOT_EXISTS);
      }
      reviewTags.add(ReviewTag.builder().name(tag).review(review).tagType(TagType.SOLO).build());
    }
    review.getReviewTags().addAll(reviewTags);

    // 리뷰 이미지 저장
    if (files != null) {
      if (files.size() > 5) {
        throw new CustomException(ErrorCode.TOO_MANY_REVIEW_IMAGES);
      }

      List<ReviewImage> reviewImages = new ArrayList<>();
      for (MultipartFile file : files) {
        if (file.getSize() > 5 * 1024 * 1024) {
          throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
        String imageUrl = s3Service.uploadReviewImage(file);
        ReviewImage reviewImage = ReviewImage.builder().imageUrl(imageUrl).review(review).build();
        reviewImages.add(reviewImage);
      }
      review.getReviewImages().addAll(reviewImages);
    }

    reviewRepository.save(review);
    reviewRepository.flush(); // 강제로 insert 쿼리 실행

    // 리뷰 평점 계산
    place.updateRating(reviewRepository.findAverageRatingByPlaceId(place.getId()));
  }
}
