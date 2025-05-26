package com.ilta.solepli.domain.sollect.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.sollect.dto.SollectSearchResponseContent;
import com.ilta.solepli.domain.sollect.dto.request.SollectCreateRequest;
import com.ilta.solepli.domain.sollect.dto.request.SollectUpdateRequest;
import com.ilta.solepli.domain.sollect.dto.response.SollectCreateResponse;
import com.ilta.solepli.domain.sollect.dto.response.SollectSearchResponse;
import com.ilta.solepli.domain.sollect.entity.ContentType;
import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.entity.SollectContent;
import com.ilta.solepli.domain.sollect.entity.mapping.SollectPlace;
import com.ilta.solepli.domain.sollect.repository.SollectContentRepository;
import com.ilta.solepli.domain.sollect.repository.SollectPlaceRepository;
import com.ilta.solepli.domain.sollect.repository.SollectRepository;
import com.ilta.solepli.domain.sollect.repository.SollectRepositoryCustom;
import com.ilta.solepli.domain.solmark.sollect.repository.SolmarkSollectRepository;
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
  private final RedisTemplate<String, Object> redisTemplate;
  private final SollectRepositoryCustom sollectRepositoryCustom;
  private final SolmarkSollectRepository solmarkSollectRepository;

  private static final String RECENT_SEARCH_PREFIX = "sollect_recent_search:";
  private static final int MAX_RECENT_SEARCH = 10;

  @Transactional
  public SollectCreateResponse createSollect(SollectCreateRequest request, User user) {

    // Sollect 저장
    Sollect sollect = Sollect.builder().title(request.title()).user(user).build();

    sollectRepository.save(sollect);
    sollectRepository.flush();

    // Sollect Place 저장
    List<SollectPlace> sollectPlaces =
        IntStream.range(0, request.placeIds().size())
            .mapToObj(
                i -> {
                  Long placeId = request.placeIds().get(i);
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));

                  return SollectPlace.builder().sollect(sollect).place(place).seq(i).build();
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
        IntStream.range(0, request.placeIds().size())
            .mapToObj(
                i -> {
                  Long placeId = request.placeIds().get(i);
                  Place place =
                      placeRepository
                          .findById(placeId)
                          .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));

                  return SollectPlace.builder().sollect(sollect).place(place).seq(i).build();
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

  /**
   * 사용자가 검색한 키워드를 ZSET에 추가하고, 최대 저장 개수를 초과한 오래된 항목은 삭제한다.
   *
   * @param userId 사용자 식별자
   * @param keyword 검색어
   */
  public void addRecentSearch(String userId, String keyword) {
    String key = keyBuild(userId);
    long score = System.currentTimeMillis();

    // ZSET에 (키워드, timestamp) 쌍으로 추가
    redisTemplate.opsForZSet().add(key, keyword, score);

    // 최신 MAX_RECENT_SEARCH개를 제외한 나머지(가장 오래된) 삭제
    redisTemplate.opsForZSet().removeRange(key, 0, -MAX_RECENT_SEARCH - 1);
  }

  /**
   * 사용자의 최근 검색어를 최신순으로 조회한다.
   *
   * @param userId 사용자 식별자
   * @return 최대 MAX_RECENT_SEARCH개까지의 검색어 리스트
   */
  public List<String> getRecentSearch(String userId) {
    String key = keyBuild(userId);

    // ZSET을 score 내림차순으로 조회
    Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, 0, MAX_RECENT_SEARCH - 1);

    if (range == null || range.isEmpty()) {
      return Collections.emptyList();
    }

    // Object → String 변환
    return range.stream().map(Object::toString).toList();
  }

  /**
   * 사용자의 특정 검색어를 ZSET에서 제거한다. 존재하지 않는 키워드면 404 예외를 던진다.
   *
   * @param userId 사용자 식별자
   * @param keyword 삭제할 검색어
   */
  public void deleteRecentSearch(String userId, String keyword) {
    String key = keyBuild(userId);

    Long removed = redisTemplate.opsForZSet().remove(key, keyword);

    // 삭제결과가 없거나 null 이면 예외 발생
    if (removed == null || removed == 0) {
      throw new CustomException(ErrorCode.RECENT_SEARCH_NOT_FOUND);
    }
  }

  @Transactional(readOnly = true)
  public SollectSearchResponse getSearchContents(
      User user, Long cursorId, int size, String keyword, String category) {

    List<SollectSearchResponseContent> rawContents =
        sollectRepositoryCustom.searchSollectByKeywordOrCategory(cursorId, size, keyword, category);

    boolean hasNext = rawContents.size() > size;
    if (hasNext) rawContents.remove(size); // 커서 페이징이므로 초과 1개 제거

    Set<Long> markedSet =
        (user == null)
            ? Collections.emptySet()
            : new HashSet<>(solmarkSollectRepository.findSollectIdsByUser(user));

    List<SollectSearchResponse.SollectSearchContent> converted =
        toResponseContent(rawContents, markedSet);

    Long nextCursorId = hasNext ? converted.get(converted.size() - 1).sollectId() : null;

    return SollectSearchResponse.builder()
        .contents(converted)
        .cursorInfo(
            SollectSearchResponse.CursorInfo.builder()
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .build())
        .build();
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

  /**
   * Redis에 저장할 키를 생성한다.
   *
   * @param userId 사용자 식별자
   * @return "recent_search:{userId}" 형태의 최종 키
   */
  private String keyBuild(String userId) {
    return RECENT_SEARCH_PREFIX + userId;
  }

  private List<SollectSearchResponse.SollectSearchContent> toResponseContent(
      List<SollectSearchResponseContent> contents, Set<Long> markedSollectIds) {
    return contents.stream()
        .map(
            content ->
                SollectSearchResponse.SollectSearchContent.builder()
                    .sollectId(content.sollectId())
                    .thumbnailImage(content.thumbnailImage())
                    .title(content.title())
                    .district(content.district())
                    .neighborhood(content.neighborhood())
                    .isMarked(markedSollectIds.contains(content.sollectId()))
                    .build())
        .toList();
  }
}
