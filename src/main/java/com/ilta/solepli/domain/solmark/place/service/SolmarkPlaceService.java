package com.ilta.solepli.domain.solmark.place.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.solmark.place.dto.reqeust.CreateCollectionRequest;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.solmark.place.repository.SolmarkPlaceCollectionRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.util.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class SolmarkPlaceService {

  private final SolmarkPlaceCollectionRepository solmarkPlaceCollectionRepository;

  @Transactional
  public void createCollection(
      CustomUserDetails customUserDetails, CreateCollectionRequest createCollectionRequest) {

    User user = customUserDetails.user();

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
