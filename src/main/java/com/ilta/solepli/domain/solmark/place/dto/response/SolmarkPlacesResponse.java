package com.ilta.solepli.domain.solmark.place.dto.response;

import java.util.List;

public record SolmarkPlacesResponse(List<SolmarkPlaceDto> places, int placeCount) {
  public static SolmarkPlacesResponse of(List<SolmarkPlaceDto> places, int placeCount) {
    return new SolmarkPlacesResponse(places, placeCount);
  }
}
