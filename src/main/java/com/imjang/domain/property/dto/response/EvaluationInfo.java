package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.PriceEvaluation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매물 평가 정보")
public record EvaluationInfo(
        @Schema(description = "즉시 입주 가능 여부", example = "true")
        boolean moveInAvailable,

        @Schema(description = "재방문 의사", example = "true")
        boolean revisitIntention,

        @Schema(description = "가격 평가", example = "REASONABLE")
        PriceEvaluation priceEvaluation
) {

}
