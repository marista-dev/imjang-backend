package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 정보")
public record PriceInfoResponse(
        @Schema(description = "보증금", example = "350000000")
        Long deposit,

        @Schema(description = "월세", example = "800000")
        Long monthlyRent,

        @Schema(description = "매매가", example = "850000000")
        Long price
) {

}
