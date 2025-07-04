package com.imjang.domain.property.dto.request;

import com.imjang.domain.property.entity.PriceEvaluation;
import com.imjang.domain.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "매물 빠른 기록 요청")
public record CreatePropertyRequest(
        @Schema(description = "주소", example = "서울시 강남구 역삼동 123-45")
        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 200, message = "주소는 200자 이내여야 합니다")
        String address,

        @Schema(description = "위도", example = "37.5012")
        @NotNull(message = "위도는 필수입니다")
        BigDecimal latitude,

        @Schema(description = "경도", example = "127.0396")
        @NotNull(message = "경도는 필수입니다")
        BigDecimal longitude,

        @Schema(description = "평점", example = "4")
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다")
        Integer rating,

        @Schema(description = "가격 평가", example = "REASONABLE")
        @NotNull(message = "가격 평가는 필수입니다")
        PriceEvaluation priceEvaluation,

        @Schema(description = "즉시 입주 가능", example = "true")
        @NotNull(message = "즉시 입주 가능 여부는 필수입니다")
        Boolean moveInAvailable,

        @Schema(description = "재방문 의사", example = "true")
        @NotNull(message = "재방문 의사는 필수입니다")
        Boolean revisitIntention,

        @Schema(description = "매매가", example = "500000000")
        Long price,

        @Schema(description = "가격 유형", example = "MONTHLY")
        @NotNull(message = "가격 유형은 필수입니다")
        PropertyType priceType,

        @Schema(description = "보증금", example = "10000000")
        Long deposit,

        @Schema(description = "월세", example = "500000")
        Long monthlyRent,

        @Schema(description = "현재층", example = "5")
        @NotNull(message = "현재층은 필수입니다")
        @Min(value = 1, message = "층수는 1 이상이어야 합니다")
        Integer currentFloor,

        @Schema(description = "총층", example = "15")
        @NotNull(message = "총층은 필수입니다")
        @Min(value = 1, message = "층수는 1 이상이어야 합니다")
        Integer totalFloors,

        @Schema(description = "평수", example = "25")
        @NotNull(message = "평수는 필수입니다")
        @Min(value = 1, message = "평수는 1 이상이어야 합니다")
        Integer area,

        @Schema(description = "관리비", example = "150000")
        Long maintenanceFee,

        @Schema(description = "이미지 ID 목록", example = "[1234, 5678]")
        @NotNull(message = "이미지는 최소 1장 이상 필요합니다")
        @Size(min = 1, message = "이미지는 최소 1장 이상 필요합니다")
        List<Long> imageIds,

        @Schema(description = "메모", example = "역세권, 관리 잘됨, 베란다 확장")
        @Size(max = 1000, message = "메모는 1000자 이내여야 합니다")
        String memo

) {

}
