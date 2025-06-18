package com.ilta.solepli.domain.solroute.dto.response;

import java.util.List;

import lombok.Builder;

import com.ilta.solepli.domain.solroute.entity.SolroutePlace;

@Builder
public record SolrouteDetailResponse(
    Long id,
    Integer iconId,
    String name,
    Integer placeCount,
    String status,
    List<PlaceInfo> placeInfos) {
  @Builder
  public record PlaceInfo(
      Integer seq,
      String placeName,
      String detailedCategory,
      String address,
      String memo,
      Double latitude,
      Double longitude) {
    public static PlaceInfo from(SolroutePlace solroutePlace) {
      return PlaceInfo.builder()
          .seq(solroutePlace.getSeq())
          .placeName(solroutePlace.getPlace().getName())
          .detailedCategory(solroutePlace.getPlace().getTypes())
          .address(solroutePlace.getPlace().getAddress())
          .memo(solroutePlace.getMemo())
          .latitude(solroutePlace.getPlace().getLatitude())
          .longitude(solroutePlace.getPlace().getLongitude())
          .build();
    }
  }
}
