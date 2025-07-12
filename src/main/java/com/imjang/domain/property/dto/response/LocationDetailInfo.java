package com.imjang.domain.property.dto.response;

import com.imjang.domain.property.entity.LocationFetchStatus;
import com.imjang.domain.property.location.dto.AmenityInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record LocationDetailInfo(
        @Schema(description = "지하철 정보")
        StationInfo subway,

        @Schema(description = "버스 정보")
        StationInfo bus,

        @Schema(description = "편의시설 정보 목록")
        List<AmenityInfo> amenities,

        @Schema(description = "위치 정보 조회 상태", example = "COMPLETED")
        LocationFetchStatus fetchStatus,

        @Schema(description = "마지막 조회 시간", example = "2024-12-28T10:31:00")
        LocalDateTime lastFetchedAt
) {

}
