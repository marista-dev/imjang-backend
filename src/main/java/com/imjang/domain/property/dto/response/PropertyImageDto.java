package com.imjang.domain.property.dto.response;

public record PropertyImageDto(
        Long imageId,
        String thumbnailUrl,
        String originalUrl
) {}