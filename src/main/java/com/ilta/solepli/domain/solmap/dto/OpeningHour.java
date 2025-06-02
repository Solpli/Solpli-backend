package com.ilta.solepli.domain.solmap.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record OpeningHour(
    Integer dayOfWeek,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime endTime) {}
