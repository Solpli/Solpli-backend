package com.ilta.solepli.domain.solroute.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.repository.PlaceRepository;
import com.ilta.solepli.domain.solroute.dto.request.SolrouteCreateRequest;
import com.ilta.solepli.domain.solroute.dto.request.SolrouteCreateRequest.PlaceInfo;
import com.ilta.solepli.domain.solroute.entity.Solroute;
import com.ilta.solepli.domain.solroute.entity.SolroutePlace;
import com.ilta.solepli.domain.solroute.repository.SolroutePlaceRepository;
import com.ilta.solepli.domain.solroute.repository.SolrouteRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolrouteService {

  private final SolrouteRepository solrouteRepository;
  private final SolroutePlaceRepository solroutePlaceRepository;
  private final PlaceRepository placeRepository;

  @Transactional
  public void createSolroute(User user, SolrouteCreateRequest request) {

    Solroute solroute =
        Solroute.builder().iconId(request.iconId()).name(request.name()).user(user).build();

    List<PlaceInfo> placeInfos = request.placeInfos();
    for (PlaceInfo placeInfo : placeInfos) {
      Place place =
          placeRepository
              .findById(placeInfo.placeId())
              .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_EXISTS));

      SolroutePlace solroutePlace =
          SolroutePlace.builder().place(place).memo(placeInfo.memo()).seq(placeInfo.seq()).build();

      solroute.addSolroutePlace(solroutePlace); // 연관관계 & 리스트 추가
    }

    solrouteRepository.save(solroute); // cascade로 SolroutePlace까지 저장
  }
}
