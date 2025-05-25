package com.ilta.solepli.domain.solmap.dto;

import java.time.LocalTime;

public record OpenStatus(boolean isOpen, LocalTime closingTime) {
  public static OpenStatus of(boolean isOpen, LocalTime closingTime) {
    return new OpenStatus(isOpen, closingTime);
  }
}
