package com.ilta.solepli.domain.solmap.dto;

public record TagInfo(String tagName, long tagTotal) {
  public static TagInfo of(String tagName, long tagTotal) {
    return new TagInfo(tagName, tagTotal);
  }
}
