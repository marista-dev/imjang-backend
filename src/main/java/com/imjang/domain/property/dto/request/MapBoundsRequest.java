package com.imjang.domain.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지도 영역 조회 요청")
public record MapBoundsRequest(
        @Schema(description = "화면 우상단 위도", example = "37.5100")
        @NotNull(message = "우상단 위도는 필수")
        @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
        @Max(value = 90, message = "위도는 90 이하여야 합니다")
        Double northEastLat,

        @Schema(description = "화면 우상단 경도", example = "127.0500")
        @NotNull(message = "우상단 경도는 필수")
        @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
        @Max(value = 180, message = "경도는 180 이하여야 합니다")
        Double northEastLng,

        @Schema(description = "화면 좌하단 위도", example = "37.4900")
        @NotNull(message = "좌하단 위도는 필수")
        @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
        @Max(value = 90, message = "위도는 90 이하여야 합니다")
        Double southWestLat,

        @Schema(description = "화면 좌하단 경도", example = "127.0300")
        @NotNull(message = "좌하단 경도는 필수")
        @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
        @Max(value = 180, message = "경도는 180 이하여야 합니다")
        Double southWestLng,

        @Schema(description = "현재 지도 줌 레벨", example = "15")
        @NotNull(message = "줌 레벨은 필수")
        @Min(value = 1, message = "줌 레벨은 1 이상이어야 합니다")
        @Max(value = 21, message = "줌 레벨은 21 이하여야 합니다")
        Integer zoomLevel
) {

}
