package com.ilta.solepli.domain.solmap.dto;

public record CursorInfo(Long id, Double distance) {
  public static CursorInfo of(Long id, Double distance) {
    return new CursorInfo(id, distance);
  }
}
