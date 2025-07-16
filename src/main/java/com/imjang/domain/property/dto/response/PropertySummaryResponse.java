package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "매물 요약 정보")
public record PropertySummaryResponse(
        @Schema(description = "매물 ID", example = "123")
        Long id,

        @Schema(description = "주소", example = "서울시 강남구 역삼동 123-45")
        String address,

        @Schema(description = "방문일시", example = "2024-12-19T10:00:00")
        LocalDateTime visitedAt,

        @Schema(description = "가격 유형", example = "JEONSE")
        PropertyType priceType,

        @Schema(description = "가격 정보")
        PriceInfoResponse priceInfo,

        @Schema(description = "평점", example = "4")
        Integer rating,

        @Schema(description = "썸네일 이미지 URL", example = "/temp-images/2024/12/19/thumb_abc123.jpg")
        String thumbnailUrl
) {

}
