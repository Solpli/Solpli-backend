package com.ilta.solepli.domain.solmark.place.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.AddSolmarkPlaceRequest;
import com.ilta.solepli.domain.solmark.place.dto.reqeust.CreateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlace;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceCollectionRepository;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolmarkPlaceService {

  private final SolmarkPlaceCollectionRepository solmarkPlaceCollectionRepository;
  private final PlaceRepository placeRepository;
  private final SolmarkPlaceRepository solmarkPlaceRepository;

  @Transactional
  public void createCollection(
      CustomUserDetails customUserDetails, CreateCollectionRequest createCollectionRequest) {

    User user = customUserDetails.user();

    // 사용자 저장 리스트 개수 조회
    Integer count = solmarkPlaceCollectionRepository.countByUser(user);

    // 최대 개수(50) 검증
    if (count >= 50) {
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
    List<SolmarkPlaceCollection> collections =
        solmarkPlaceCollectionRepository.findByUserAndId_In(user, collectionIds);

    // 조회한 쏠마크 저장 리스트에 추가할 SolmarkPlace 객체 리스트
    List<SolmarkPlace> solmarkPlaces =
        collections.stream()
            .map(c -> SolmarkPlace.builder().solmarkPlaceCollection(c).place(place).build())
            .toList();

    solmarkPlaceRepository.saveAll(solmarkPlaces);
  }
}
