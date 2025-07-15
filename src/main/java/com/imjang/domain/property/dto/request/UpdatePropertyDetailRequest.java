package com.imjang.domain.property.dto.request;

import com.imjang.domain.property.entity.EnvironmentType;
import com.imjang.domain.property.entity.ParkingType;
import com.imjang.domain.property.entity.PriceEvaluation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(description = "매물 상세 정보 수정 요청")
public record UpdatePropertyDetailRequest(
        @Schema(description = "즉시 입주 가능 여부", example = "true")
        Boolean moveInAvailable,

        @Schema(description = "재방문 의사", example = "true")
        Boolean revisitIntention,

        @Schema(description = "가격 평가", example = "REASONABLE")
        PriceEvaluation priceEvaluation,

        @Schema(description = "주차 가능 여부", example = "AVAILABLE")
        ParkingType parkingType,

        @Schema(description = "관리비", example = "150000")
        Long maintenanceFee,

        @Schema(description = "환경 특성 목록", example = "[\"QUIET\", \"NEAR_PARK\"]")
        Set<EnvironmentType> environments,

        @Schema(description = "메모", example = "역세권, 관리 잘됨, 베란다 확장")
        @Size(max = 1000, message = "메모는 1000자 이내여야 합니다")
        String memo
) {

}
