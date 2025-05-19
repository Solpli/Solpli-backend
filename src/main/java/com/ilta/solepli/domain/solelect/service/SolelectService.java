package com.ilta.solepli.domain.solelect.service;

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
import com.ilta.solepli.domain.solelect.dto.request.SolelectCreateRequest;
import com.ilta.solepli.domain.solelect.dto.request.SolelectUpdateRequest;
import com.ilta.solepli.domain.solelect.dto.response.SolelectCreateResponse;
import com.ilta.solepli.domain.solelect.entity.ContentType;
import com.ilta.solepli.domain.solelect.entity.Solelect;
import com.ilta.solepli.domain.solelect.entity.SolelectContent;
import com.ilta.solepli.domain.solelect.entity.mapping.SolelectPlace;
import com.ilta.solepli.domain.solelect.repository.SolelectContentRepository;
import com.ilta.solepli.domain.solelect.repository.SolelectPlaceRepository;
import com.ilta.solepli.domain.solelect.repository.SolelectRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;
import com.ilta.solepli.global.service.S3Service;

@Service
@RequiredArgsConstructor
public class SolelectService {

  private final SolelectRepository solelectRepository;
  private final SolelectPlaceRepository solelectPlaceRepository;
  private final SolelectContentRepository solelectContentRepository;
  private final PlaceRepository placeRepository;
  private final S3Service s3Service;

  @Transactional
  public SolelectCreateResponse createSolelect(SolelectCreateRequest request, User user) {

    // Solelect 저장
    Solelect solelect = Solelect.builder().title(request.title()).user(user).build();

    solelectRepository.save(solelect);
    solelectRepository.flush();

    // Solelect Place 저장
    List<SolelectPlace> solelectPlaces =
        request.placeIds().stream()
            .map(
                placeId -> {
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));
                  return SolelectPlace.builder().solelect(solelect).place(place).build();
                })
            .collect(Collectors.toList());

    solelectPlaceRepository.saveAll(solelectPlaces);

    // Solelect Content 저장
    List<SolelectContent> solelectContents = new ArrayList<>();
    List<SolelectCreateRequest.SolelectContent> contentList = request.contents();
    for (SolelectCreateRequest.SolelectContent content : contentList) {
      SolelectContent solelectContent = null;
      if (content.type().equals(ContentType.TEXT)) {
        solelectContent =
            SolelectContent.builder()
                .solelect(solelect)
                .seq(content.seq())
                .type(content.type())
                .text(content.content())
                .build();
      } else if (content.type().equals(ContentType.IMAGE)) {
        solelectContent =
            SolelectContent.builder()
                .solelect(solelect)
                .seq(content.seq())
                .type(content.type())
                .filename(content.content())
                .build();
      }
      solelectContents.add(solelectContent);
    }

    solelectContentRepository.saveAll(solelectContents);

    return SolelectCreateResponse.builder().solelectId(solelect.getId()).build();
  }

  @Transactional
  public void uploadSolelectImage(Long id, List<MultipartFile> files, User user) {

    Solelect solelect =
        solelectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLELECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!solelect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLELECT_FORBIDDEN);
    }

    List<SolelectContent> solelectContents = solelect.getSolelectContents();

    if (files.size() > 100) {
      throw new CustomException(ErrorCode.TOO_MANY_SOLELECT_IMAGES);
    }

    for (MultipartFile file : files) {
      String filename = file.getOriginalFilename();
      SolelectContent solelectContent = findImage(solelectContents, filename);
      if (solelectContent == null) {
        throw new CustomException(ErrorCode.CONTENT_IMAGE_NOT_FOUND);
      }
      String imageUrl = s3Service.uploadSolelectImage(file);
      solelectContent.updateImageUrl(imageUrl);
      if (solelectContent.getSeq() == 0) {
        solelectContent.updateIsThumbnail(true);
      }
    }
  }

  @Transactional
  public void updateSolelect(Long id, SolelectUpdateRequest request, User user) {

    Solelect solelect =
        solelectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLELECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!solelect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLELECT_FORBIDDEN);
    }

    // 기존 쏠렉트 장소와 쏠렉트 콘텐츠 삭제 후 다시 저장
    deleteS3Images(solelect.getSolelectContents());
    solelectPlaceRepository.deleteBySolelect(solelect);
    solelectPlaceRepository.flush();
    solelectContentRepository.deleteBySolelect(solelect);
    solelectContentRepository.flush();

    solelect.updateTitle(request.title());

    // Solelect Place 저장
    List<SolelectPlace> solelectPlaces =
        request.placeIds().stream()
            .map(
                placeId -> {
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));
                  return SolelectPlace.builder().solelect(solelect).place(place).build();
                })
            .collect(Collectors.toList());

    solelectPlaceRepository.saveAll(solelectPlaces);

    // Solelect Content 저장
    List<SolelectContent> solelectContents = new ArrayList<>();
    List<SolelectUpdateRequest.SolelectContent> contentList = request.contents();
    for (SolelectUpdateRequest.SolelectContent content : contentList) {
      SolelectContent solelectContent = null;
      if (content.type().equals(ContentType.TEXT)) {
        solelectContent =
            SolelectContent.builder()
                .solelect(solelect)
                .seq(content.seq())
                .type(content.type())
                .text(content.content())
                .build();
      } else if (content.type().equals(ContentType.IMAGE)) {
        solelectContent =
            SolelectContent.builder()
                .solelect(solelect)
                .seq(content.seq())
                .type(content.type())
                .filename(content.content())
                .build();
      }
      solelectContents.add(solelectContent);
    }

    solelectContentRepository.saveAll(solelectContents);
  }

  @Transactional
  public void deleteSolelect(Long id, User user) {
    Solelect solelect =
        solelectRepository
            .findWithContentById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLELECT_NOT_FOUND));

    // 쏠렉트 소유자가 맞는지 검증
    if (!solelect.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.SOLELECT_FORBIDDEN);
    }

    // 추후에 S3 용량 문제 발생시 다시 활성화
    // deleteS3Images(solelect.getSolelectContents());

    solelect.softDelete();
    // solelectRepository.delete(solelect);
  }

  private SolelectContent findImage(List<SolelectContent> solelectContents, String filename) {
    for (SolelectContent solelectContent : solelectContents) {
      if (solelectContent.getType().equals(ContentType.IMAGE)
          && solelectContent.getFilename().equals(filename)) {
        return solelectContent;
      }
    }
    return null;
  }

  private void deleteS3Images(List<SolelectContent> solelectContents) {
    solelectContents.stream()
        .filter(content -> content.getType() == ContentType.IMAGE)
        .map(SolelectContent::getImageUrl)
        .filter(Objects::nonNull) // null인 경우 필터링
        .forEach(s3Service::deleteSolelectImage);
  }
}
