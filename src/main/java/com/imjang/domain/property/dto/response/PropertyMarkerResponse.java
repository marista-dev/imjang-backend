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

        @Schema(description = "마커 색상", example = "GREEN")
        MarkerColor markerColor,

        @Schema(description = "해당 위치의 매물 수", example = "1")
        Integer count
) {

  public static PropertyMarkerResponse from(Property property) {
    return new PropertyMarkerResponse(
            property.getId(),
            property.getLatitude(),
            property.getLongitude(),
            getMarkerColor(property.getRating()),
            1
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
