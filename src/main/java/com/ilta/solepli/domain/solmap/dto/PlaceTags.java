package com.ilta.solepli.domain.solmap.dto;

import java.util.List;

public record PlaceTags(List<TagInfo> mood, List<TagInfo> solo) {
  public static PlaceTags of(List<TagInfo> mood, List<TagInfo> solo) {
    return new PlaceTags(mood, solo);
  }
}
