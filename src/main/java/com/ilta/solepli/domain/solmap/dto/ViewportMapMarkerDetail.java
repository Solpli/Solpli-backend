package com.ilta.solepli.domain.solmap.dto;

public record ViewportMapMarkerDetail(Long id, Double latitude, Double longitude, String category) {
  public static ViewportMapMarkerDetail of(
      Long id, Double latitude, Double longitude, String category) {
    return new ViewportMapMarkerDetail(id, latitude, longitude, category);
  }
}
