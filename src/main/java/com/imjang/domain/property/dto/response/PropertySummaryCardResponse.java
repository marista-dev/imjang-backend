package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "매물 간략 정보 카드")
public record PropertySummaryCardResponse(
        @Schema(description = "매물 ID", example = "1")
        Long id,

        @Schema(description = "주소", example = "서초구 서초동 789-12")
        String address,

        @Schema(description = "가격 유형", example = "JEONSE")
        PropertyType priceType,

        @Schema(description = "보증금", example = "280000000")
        Long deposit,

        @Schema(description = "월세", example = "0")
        Long monthlyRent,

        @Schema(description = "평점", example = "4.0")
        Double rating,

        @Schema(description = "썸네일 이미지 URL", example = "/temp-images/2024/12/15/thumb_abc123.jpg")
        String thumbnailUrl,

        @Schema(description = "방문일시", example = "2024-12-15T14:30:00")
        LocalDateTime visitedAt
) {

}
