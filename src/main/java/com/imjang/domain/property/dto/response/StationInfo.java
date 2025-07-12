package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역/정류장 정보")
public record StationInfo(
        @Schema(description = "가장 가까운 역/정류장", example = "역삼역")
        String nearestStation,

        @Schema(description = "거리 (미터)", example = "650")
        Integer distance,

        @Schema(description = "도보 시간 (분)", example = "8")
        Integer walkTime
) {

}
