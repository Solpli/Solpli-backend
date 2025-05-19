package com.ilta.solepli.domain.solmap.dto;

import java.util.List;

public record ViewportMapMarkerResponse(
    List<ViewportMapMarkerDetail> places, List<String> categories) {
  public static ViewportMapMarkerResponse of(
      List<ViewportMapMarkerDetail> viewportMapMarkerDetails, List<String> categories) {
    return new ViewportMapMarkerResponse(viewportMapMarkerDetails, categories);
  }
}
