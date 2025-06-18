package com.ilta.solepli.domain.solmark.place.dto.reqeust;

import java.util.List;

public record UpdatePlaceCollectionsRequest(
    List<Long> addCollectionIds, List<Long> removeCollectionIds) {}
