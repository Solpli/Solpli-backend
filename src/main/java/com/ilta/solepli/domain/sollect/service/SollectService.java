package com.ilta.solepli.domain.sollect.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.sollect.dto.request.SollectCreateRequest;
import com.ilta.solepli.domain.sollect.dto.request.SollectUpdateRequest;
import com.ilta.solepli.domain.sollect.dto.response.SollectCreateResponse;
import com.ilta.solepli.domain.sollect.entity.ContentType;
import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.entity.SollectContent;
import com.ilta.solepli.domain.sollect.entity.mapping.SollectPlace;
import com.ilta.solepli.domain.sollect.repository.SollectContentRepository;
import com.ilta.solepli.domain.sollect.repository.SollectPlaceRepository;
import com.ilta.solepli.domain.sollect.repository.SollectRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;
import com.ilta.solepli.global.service.S3Service;

@Service
@RequiredArgsConstructor
public class SollectService {

  private final SollectRepository sollectRepository;
  private final SollectPlaceRepository sollectPlaceRepository;
  private final SollectContentRepository sollectContentRepository;
  private final PlaceRepository placeRepository;
  private final S3Service s3Service;

  @Transactional
  public SollectCreateResponse createSollect(SollectCreateRequest request, User user) {

    // Sollect 저장
    Sollect sollect = Sollect.builder().title(request.title()).user(user).build();

    sollectRepository.save(sollect);
    sollectRepository.flush();

    // Sollect Place 저장
    List<SollectPlace> sollectPlaces =
        request.placeIds().stream()
            .map(
                placeId -> {
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));
                  return SollectPlace.builder().sollect(sollect).place(place).build();
                })
            .collect(Collectors.toList());

    sollectPlaceRepository.saveAll(sollectPlaces);

    // Sollect Content 저장
    List<SollectContent> sollectContents = new ArrayList<>();
    List<SollectCreateRequest.SollectContent> contentList = request.contents();
    for (SollectCreateRequest.SollectContent content : contentList) {
      SollectContent sollectContent = null;
      if (content.type().equals(ContentType.TEXT)) {
        sollectContent =
            SollectContent.builder()
                .sollect(sollect)
                .seq(content.seq())
                .type(content.type())
                .text(content.content())
                .build();
      } else if (content.type().equals(ContentType.IMAGE)) {
        sollectContent =
            SollectContent.builder()
                .sollect(sollect)
                .seq(content.seq())
                .type(content.type())
                .filename(content.content())
                .build();
      }
      sollectContents.add(sollectContent);
    }

    sollectContentRepository.saveAll(sollectContents);

    return SollectCreateResponse.builder().sollectId(sollect.getId()).build();
  }

  @Transactional
  public void uploadSollectImage(Long id, List<MultipartFile> files, User user) {

    Sollect sollect =
        sollectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!sollect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLLECT_FORBIDDEN);
    }

    List<SollectContent> sollectContents = sollect.getSollectContents();

    if (files.size() > 100) {
      throw new CustomException(ErrorCode.TOO_MANY_SOLLECT_IMAGES);
    }

    for (MultipartFile file : files) {
      String filename = file.getOriginalFilename();
      SollectContent sollectContent = findImage(sollectContents, filename);
      if (sollectContent == null) {
        throw new CustomException(ErrorCode.CONTENT_IMAGE_NOT_FOUND);
      }
      String imageUrl = s3Service.uploadSollectImage(file);
      sollectContent.updateImageUrl(imageUrl);
      if (sollectContent.getSeq() == 0) {
        sollectContent.updateIsThumbnail(true);
      }
    }
  }

  @Transactional
  public void updateSollect(Long id, SollectUpdateRequest request, User user) {

    Sollect sollect =
        sollectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!sollect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLLECT_FORBIDDEN);
    }

    // 기존 쏠렉트 장소와 쏠렉트 콘텐츠 삭제 후 다시 저장
    deleteS3Images(sollect.getSollectContents());
    sollectPlaceRepository.deleteBySollect(sollect);
    sollectPlaceRepository.flush();
    sollectContentRepository.deleteBySollect(sollect);
    sollectContentRepository.flush();

    sollect.updateTitle(request.title());

    // Sollect Place 저장
    List<SollectPlace> sollectPlaces =
        request.placeIds().stream()
            .map(
                placeId -> {
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));
                  return SollectPlace.builder().sollect(sollect).place(place).build();
                })
            .collect(Collectors.toList());

    sollectPlaceRepository.saveAll(sollectPlaces);

    // Sollect Content 저장
    List<SollectContent> sollectContents = new ArrayList<>();
    List<SollectUpdateRequest.SollectContent> contentList = request.contents();
    for (SollectUpdateRequest.SollectContent content : contentList) {
      SollectContent sollectContent = null;
      if (content.type().equals(ContentType.TEXT)) {
        sollectContent =
            SollectContent.builder()
                .sollect(sollect)
                .seq(content.seq())
                .type(content.type())
                .text(content.content())
                .build();
      } else if (content.type().equals(ContentType.IMAGE)) {
        sollectContent =
            SollectContent.builder()
                .sollect(sollect)
                .seq(content.seq())
                .type(content.type())
                .filename(content.content())
                .build();
      }
      sollectContents.add(sollectContent);
    }

    sollectContentRepository.saveAll(sollectContents);
  }

  @Transactional
  public void deleteSollect(Long id, User user) {
    Sollect sollect =
        sollectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!sollect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLLECT_FORBIDDEN);
    }

    // 추후에 S3 용량 문제 발생시 다시 활성화
    // deleteS3Images(sollect.getSollectContents());

    sollect.softDelete();
    // sollectRepository.delete(sollect);
  }

  private SollectContent findImage(List<SollectContent> sollectContents, String filename) {
    for (SollectContent sollectContent : sollectContents) {
      if (sollectContent.getType().equals(ContentType.IMAGE)
          && sollectContent.getFilename().equals(filename)) {
        return sollectContent;
      }
    }
    return null;
  }

  private void deleteS3Images(List<SollectContent> sollectContents) {
    sollectContents.stream()
        .filter(content -> content.getType() == ContentType.IMAGE)
        .map(SollectContent::getImageUrl)
        .filter(Objects::nonNull) // null인 경우 필터링
        .forEach(s3Service::deleteSollectImage);
  }
}
