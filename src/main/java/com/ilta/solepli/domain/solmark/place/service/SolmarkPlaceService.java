package com.ilta.solepli.domain.solmark.place.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.AddSolmarkPlaceRequest;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.CreateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.UpdateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.dto.response.CollectionResponse;
import com.ilta.solepli.domain.solmark.place.dto.response.SolmarkPlaceDto;
import com.ilta.solepli.domain.solmark.place.dto.response.SolmarkPlacesResponse;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlace;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceCollectionRepository;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;
import com.ilta.solepli.global.util.PlaceUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolmarkPlaceService {

  private final SolmarkPlaceCollectionRepository solmarkPlaceCollectionRepository;
  private final PlaceRepository placeRepository;
  private final SolmarkPlaceRepository solmarkPlaceRepository;

  private static final int MAX_PLACES_PER_COLLECTION = 100;
  private static final int MAX_COLLECTIONS_PER_USER = 50;
  private static final int TAG_LIMIT = 3;

  @Transactional
  public void createCollection(
      CustomUserDetails customUserDetails, CreateCollectionRequest createCollectionRequest) {

    User user = customUserDetails.user();

    // 사용자 저장 리스트 개수 조회
    Integer count = solmarkPlaceCollectionRepository.countByUser(user);

    // 최대 개수(50) 검증
    if (count >= MAX_COLLECTIONS_PER_USER) {
      throw new CustomException(ErrorCode.COLLECTION_LIMIT_EXCEEDED);
    }

    // 쏠마크 저장 리스트 객체 생성
    SolmarkPlaceCollection placeCollection =
        SolmarkPlaceCollection.builder()
            .user(user)
            .name(createCollectionRequest.collectionName())
            .iconId(createCollectionRequest.iconId())
            .build();

    solmarkPlaceCollectionRepository.save(placeCollection);
  }

  @Transactional
  public void addSolmarkPlace(
      CustomUserDetails customUserDetails, AddSolmarkPlaceRequest addSolmarkPlaceRequest) {
    User user = customUserDetails.user();
    List<Long> collectionIds = addSolmarkPlaceRequest.collectionIds();
    // 추가할 장소 조회
    Place place =
        placeRepository
            .findById(addSolmarkPlaceRequest.placeId())
            .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));

    // 요청 데이터 collectionIds를 기반으로 사용자 쏠마크 저장 리스트 조회
    List<SolmarkPlaceCollection> collections = validateAndGetCollections(user, collectionIds);

    // 추가할 저장 리스트에 이미 저장된 장소인지 검증
    validateCollectionPlaceDuplicate(collections, place);

    // 조회한 쏠마크 저장 리스트에 추가할 SolmarkPlace 객체 리스트
    List<SolmarkPlace> solmarkPlaces =
        collections.stream()
            .map(
                c -> {
                  // 각 장소 리스트별 최대 장소 저장 개수 검증
                  validateCollectionPlaceLimit(c);

                  return SolmarkPlace.builder().solmarkPlaceCollection(c).place(place).build();
                })
            .toList();

    solmarkPlaceRepository.saveAll(solmarkPlaces);
  }

  private List<SolmarkPlaceCollection> validateAndGetCollections(User user, List<Long> ids) {
    List<SolmarkPlaceCollection> cols =
        solmarkPlaceCollectionRepository.findByUserAndId_In(user, ids);
    // 저장 리스트 ID 검증
    if (cols.size() != ids.size()) {
      throw new CustomException(ErrorCode.COLLECTION_NOT_FOUND);
    }
    return cols;
  }

  private void validateCollectionPlaceLimit(SolmarkPlaceCollection c) {
    if (c.getSolmarkPlaces().size() >= MAX_PLACES_PER_COLLECTION) {
      throw new CustomException(ErrorCode.EXCEEDED_MARK_PLACE_LIMIT);
    }
  }

  private void validateCollectionPlaceDuplicate(
      List<SolmarkPlaceCollection> collections, Place place) {
    if (solmarkPlaceRepository.existsBySolmarkPlaceCollectionInAndPlace(collections, place)) {
      throw new CustomException(ErrorCode.DUPLICATED_MARK_PLACE);
    }
  }

  @Transactional(readOnly = true)
  public List<CollectionResponse> getCollections(CustomUserDetails customUserDetails) {
    User user = customUserDetails.user();

    // 사용자 쏠마크 -장소 저장 리스트 조회
    List<SolmarkPlaceCollection> solmarkPlaceCollections =
        solmarkPlaceCollectionRepository.findByUser(user);

    return solmarkPlaceCollections.stream()
        // CollectionResponse DTO 매핑
        .map(this::mapToCollectionResponse)
        .toList();
  }

  private CollectionResponse mapToCollectionResponse(SolmarkPlaceCollection spc) {
    int placeCount = spc.getSolmarkPlaces().size();

    return CollectionResponse.builder()
        .collectionId(spc.getId())
        .iconId(spc.getIconId())
        .collectionName(spc.getName())
        .placeCount(placeCount)
        .build();
  }

  @Transactional(readOnly = true)
  public SolmarkPlacesResponse getSolmarkPlaces(
      CustomUserDetails customUserDetails, Long collectionId) {

    // 특정 쏠마크 장소 저장 리스트(collectionId) 조회
    List<SolmarkPlace> solmarkplaces =
        solmarkPlaceRepository.findByUserAndCollectionId(customUserDetails.user(), collectionId);
    // 쏠마크 장소 개수
    int placeCount = solmarkplaces.size();

    // 쏠마크 장소 조회 DTO 매핑
    List<SolmarkPlaceDto> solmarkPlaceDtos =
        solmarkplaces.stream().map(this::mapToSolmarkPlaceDto).toList();

    return SolmarkPlacesResponse.of(solmarkPlaceDtos, placeCount);
  }

  private SolmarkPlaceDto mapToSolmarkPlaceDto(SolmarkPlace sp) {
    Long placeId = sp.getPlace().getId();
    String name = sp.getPlace().getName();
    String detailedCategory = sp.getPlace().getTypes();
    Integer recommendationPercent = placeRepository.getRecommendationPercent(placeId);
    List<String> tags = placeRepository.getTopTagsForPlace(placeId, TAG_LIMIT);
    Double rating = PlaceUtil.truncateTo2Decimals(sp.getPlace().getRating());

    return SolmarkPlaceDto.builder()
        .PlaceId(placeId)
        .name(name)
        .detailedCategory(detailedCategory)
        .recommendationPercent(recommendationPercent)
        .tags(tags)
        .rating(rating)
        .build();
  }

  @Transactional
  public void updateCollection(
      CustomUserDetails customUserDetails,
      Long collectionId,
      UpdateCollectionRequest updateCollectionRequest) {
    User user = customUserDetails.user();

    // 사용자의 쏠마크 저장 리스트 조회
    SolmarkPlaceCollection solmarkPlaceCollection =
        solmarkPlaceCollectionRepository
            .findByIdAndUser(collectionId, user)
            .orElseThrow(() -> new CustomException(ErrorCode.COLLECTION_NOT_FOUND));

    // 수정할 저장 리스트 이름과 아이콘 번호
    String updateName = updateCollectionRequest.collectionName();
    Integer updateIconId = updateCollectionRequest.iconId();

    // 쏠마크 저장 리스트 수정
    solmarkPlaceCollection.updateInfo(updateName, updateIconId);
  }

  @Transactional
  public void deleteCollection(CustomUserDetails customUserDetails, Long collectionId) {
    User user = customUserDetails.user();

    // 쏠마크 저장 리스트 삭제
    solmarkPlaceCollectionRepository
        .findByIdAndUser(collectionId, user)
        .orElseThrow(() -> new CustomException(ErrorCode.COLLECTION_NOT_FOUND))
        .softDelete(); // delete 시점 기록
  }
}
