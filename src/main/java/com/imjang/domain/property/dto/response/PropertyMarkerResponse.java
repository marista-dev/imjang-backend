package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.MarkerColor;
import com.imjang.domain.property.entity.Property;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마커 매물 정보")
public record PropertyMarkerResponse(
        @Schema(description = "매물 ID", example = "1")
        Long id,

        @Schema(description = "위도", example = "37.5012")
        Double latitude,

        @Schema(description = "경도", example = "127.0396")
        Double longitude,

        @Schema(description = "주소", example = "서초구 서초동 789-12")
        String address,

        @Schema(description = "가격 유형", example = "JEONSE")
        String priceType,

        @Schema(description = "보증금", example = "280000000")
        Long deposit,

        @Schema(description = "월세", example = "0")
        Long monthlyRent,

        @Schema(description = "매매가", example = "0")
        Long price,

        @Schema(description = "평점", example = "4")
        Integer rating,

        @Schema(description = "마커 색상", example = "GREEN")
        MarkerColor markerColor,

        @Schema(description = "썸네일 URL")
        String thumbnailUrl
) {

  public static PropertyMarkerResponse from(Property property, String thumbnailUrl) {
    return new PropertyMarkerResponse(
            property.getId(),
            property.getLatitude(),
            property.getLongitude(),
            property.getAddress(),
            property.getPriceType().name(),
            property.getDeposit(),
            property.getMonthlyRent(),
            property.getPrice(),
            property.getRating(),
            getMarkerColor(property.getRating()),
            thumbnailUrl
    );
  }

  private static MarkerColor getMarkerColor(Integer rating) {
    if (rating >= 4) {
      return MarkerColor.GREEN;
    } else if (rating == 3) {
      return MarkerColor.YELLOW;
    } else {
      return MarkerColor.RED;
    }
  }
}
