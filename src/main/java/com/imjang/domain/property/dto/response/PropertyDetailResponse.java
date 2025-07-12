package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "매물 상세 정보 응답")
public record PropertyDetailResponse(
        @Schema(description = "매물 ID", example = "1")
        Long id,

        @Schema(description = "주소", example = "강남구 역삼동 123-45")
        String address,

        @Schema(description = "등록일시", example = "2024-12-28T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "가격 유형", example = "MONTHLY")
        PropertyType priceType,

        @Schema(description = "보증금", example = "10000000")
        Long deposit,

        @Schema(description = "월세", example = "500000")
        Long monthlyRent,

        @Schema(description = "매매가", example = "null")
        Long price,

        @Schema(description = "관리비", example = "150000")
        Long maintenanceFee,

        @Schema(description = "평수", example = "25")
        Integer area,

        @Schema(description = "현재층", example = "5")
        Integer currentFloor,

        @Schema(description = "총층", example = "15")
        Integer totalFloor,

        @Schema(description = "평점", example = "4")
        Integer rating,

        @Schema(description = "이미지 URL 목록")
        List<String> images,

        @Schema(description = "평가 정보")
        EvaluationInfo evaluation,

        @Schema(description = "메모", example = "역세권, 관리 잘됨, 베란다")
        String memo,

        @Schema(description = "위치 상세 정보")
        LocationDetailInfo locationInfo
) {

}
