package com.ilta.solepli.domain.solmark.place.dto.response;

import lombok.Builder;

@Builder
public record CollectionResponse(
    Long collectionId, int iconId, String collectionName, int placeCount) {}
