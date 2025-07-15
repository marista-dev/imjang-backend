package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.location.dto.AmenityInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LocationDetailInfo(
        @Schema(description = "지하철 정보")
        StationInfo subway,

        @Schema(description = "버스 정보")
        StationInfo bus,

        @Schema(description = "편의시설 정보 목록")
        List<AmenityInfo> amenities
) {

}
