package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "날짜별 그룹화된 매물 정보")
public record TimelineGroupResponse(
        @Schema(description = "날짜", example = "2024-12-28")
        LocalDate date,

        @Schema(description = "해당 날짜의 매물 목록")
        List<TimelinePropertyResponse> properties
) {
}
