package com.ilta.solepli.domain.place.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.place.dto.response.PlaceSearchResponse;
import com.ilta.solepli.domain.place.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
public class PlaceService {

  private final PlaceRepository placeRepository;

  @Transactional(readOnly = true)
  public List<PlaceSearchResponse> getSearchPlaces(String keyword) {
    return placeRepository.getPlacesByKeyword(keyword);
  }
}
