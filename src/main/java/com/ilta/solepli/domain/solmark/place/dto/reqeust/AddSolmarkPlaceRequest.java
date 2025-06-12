package com.ilta.solepli.domain.solmark.place.dto.reqeust;

import java.util.List;

public record AddSolmarkPlaceRequest(List<Long> collectionIds, Long placeId) {}
