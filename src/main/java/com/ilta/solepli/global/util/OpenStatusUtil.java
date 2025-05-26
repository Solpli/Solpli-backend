package com.ilta.solepli.global.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.place.entity.PlaceHour;
import com.ilta.solepli.global.dto.OpenStatus;

public class OpenStatusUtil {

  // 현재 영업중 여부 및 마감 시간 반환
  public static OpenStatus getOpenStatus(Place place) {
    int todayNow = LocalDate.now().getDayOfWeek().getValue() % 7;
    LocalTime now = LocalTime.now();

    Optional<LocalTime> endTime =
        place.getPlaceHours().stream()
            .filter(ph -> ph.getDayOfWeek() == todayNow)
            .filter(ph -> !now.isBefore(ph.getStartTime()) && !now.isAfter(ph.getEndTime()))
            .map(PlaceHour::getEndTime)
            .findFirst();

    return OpenStatus.of(endTime.isPresent(), endTime.orElse(null));
  }
}
