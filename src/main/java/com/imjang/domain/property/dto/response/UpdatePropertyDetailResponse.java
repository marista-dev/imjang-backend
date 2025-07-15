package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.EnvironmentType;
import com.imjang.domain.property.entity.ParkingType;
import com.imjang.domain.property.entity.PriceEvaluation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "매물 상세 정보 수정 응답")
public record UpdatePropertyDetailResponse(
        @Schema(description = "매물 ID", example = "123")
        Long propertyId,

        @Schema(description = "즉시 입주 가능 여부", example = "true")
        boolean moveInAvailable,

        @Schema(description = "재방문 의사", example = "true")
        boolean revisitIntention,

        @Schema(description = "가격 평가", example = "REASONABLE")
        PriceEvaluation priceEvaluation,

        @Schema(description = "주차 가능 여부", example = "AVAILABLE")
        ParkingType parkingType,

        @Schema(description = "관리비", example = "150000")
        Long maintenanceFee,

        @Schema(description = "환경 특성 목록", example = "[\"QUIET\", \"NEAR_PARK\"]")
        Set<EnvironmentType> environments,

        @Schema(description = "메모", example = "역세권, 관리 잘됨, 베란다 확장")
        String memo,

        @Schema(description = "수정일시", example = "2024-01-15T14:30:00")
        LocalDateTime updatedAt
) {

}
