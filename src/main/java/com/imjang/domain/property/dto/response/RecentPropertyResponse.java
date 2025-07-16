package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "최근 매물 목록 응답")
public record RecentPropertyResponse(
        @Schema(description = "매물 목록")
        List<PropertySummaryResponse> properties,

        @Schema(description = "전체 매물 개수", example = "45")
        long totalCount,

        @Schema(description = "이번 달 기록한 매물 개수", example = "12")
        long monthlyRecordCount
) {

}
