package com.imjang.domain.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 위치 정보 사전 수집 요청
 */
@Schema(description = "위치 정보 사전 수집 요청")
public record PrefetchLocationRequest(

        @Schema(description = "위도", example = "37.5665")
        @NotNull(message = "위도는 필수입니다")
        @Min(value = 33, message = "위도는 33.0 이상이어야 합니다")
        @Max(value = 39, message = "위도는 39.0 이하이어야 합니다")
        Double latitude,

        @Schema(description = "경도", example = "126.9780")
        @NotNull(message = "경도는 필수입니다")
        @Min(value = 124, message = "경도는 124.0 이상이어야 합니다")
        @Max(value = 132, message = "경도는 132.0 이하이어야 합니다")
        Double longitude,

        @Schema(description = "주소 (선택)", example = "서울시 강남구 역삼동")
        String address
) {

}
