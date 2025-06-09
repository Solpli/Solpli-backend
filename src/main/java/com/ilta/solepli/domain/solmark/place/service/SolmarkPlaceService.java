package com.ilta.solepli.domain.solmark.place.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmark.place.dto.reqeust.CreateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceCollectionRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolmarkPlaceService {

  private final SolmarkPlaceCollectionRepository solmarkPlaceCollectionRepository;

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
}
