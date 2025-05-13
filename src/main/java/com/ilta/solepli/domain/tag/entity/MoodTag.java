package com.ilta.solepli.domain.tag.entity;

import java.util.Arrays;

import lombok.Getter;

import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Getter
public enum MoodTag {
  QUIET("조용한"),
  COZY("시끌벅적한"),
  COMFORTABLE("편안한"),
  LUXURIOUS("고급스러운"),
  COLORFUL("색다른"),
  HIP("힙한"),
  TRENDY("트렌디한"),
  NATURAL("자연친화적인"),
  GOOD_FOR_PHOTOS("사진 찍기 좋은"),
  GOOD_VIEW("뷰가 좋은");

  private final String description;

  MoodTag(String description) {
    this.description = description;
  }

  public static MoodTag fromDescription(String description) {
    for (MoodTag tag : MoodTag.values()) {
      if (tag.getDescription().equals(description)) {
        return tag;
      }
    }
    throw new CustomException(ErrorCode.TAG_NOT_EXISTS);
  }

  public static boolean isValid(String description) {
    return Arrays.stream(values()).anyMatch(tag -> tag.getDescription().equals(description));
  }
}
