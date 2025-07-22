package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "타임라인 매물 정보")
public record TimelinePropertyResponse(
        @Schema(description = "매물 ID", example = "123")
        Long id,

        @Schema(description = "방문 일시", example = "2024-12-28T10:30:00")
        LocalDateTime visitedAt,

        @Schema(description = "주소", example = "강남구 역삼동")
        String address,

        @Schema(description = "평점", example = "4")
        Integer rating,

        @Schema(description = "가격 유형", example = "JEONSE")
        PropertyType priceType,

        @Schema(description = "보증금", example = "350000000")
        Long deposit,

        @Schema(description = "월세", example = "0")
        Long monthlyRent,

        @Schema(description = "면적(제곱미터)", example = "82.64")
        Integer area,

        @Schema(description = "층수", example = "5")
        Integer floor,

        @Schema(description = "총 층수", example = "15")
        Integer totalFloor,

        @Schema(description = "썸네일 URL", example = "/api/v1/properties/123/thumbnail")
        String thumbnailUrl
) {
}
