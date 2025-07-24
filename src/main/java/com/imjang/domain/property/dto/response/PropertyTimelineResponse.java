package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매물 타임라인 응답")
public record PropertyTimelineResponse(
        @Schema(description = "타임라인 데이터")
        List<TimelineGroupResponse> timelineGroups,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

}
