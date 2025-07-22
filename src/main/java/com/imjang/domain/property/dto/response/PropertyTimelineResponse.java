package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매물 타임라인 응답")
public record PropertyTimelineResponse(
        @Schema(description = "응답 코드", example = "200")
        int code,

        @Schema(description = "타임라인 데이터")
        List<TimelineGroupResponse> data,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

  public static PropertyTimelineResponse of(List<TimelineGroupResponse> data, boolean hasNext) {
    return new PropertyTimelineResponse(200, data, hasNext);
  }
}
