package com.imjang.domain.property.event;

public record LocationPrefetchEvent(
        Double latitude,
        Double longitude,
        String address,
        Long userId
) {
}
