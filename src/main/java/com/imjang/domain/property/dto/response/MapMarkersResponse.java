package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지도 마커 목록 응답")
public record MapMarkersResponse(
        @Schema(description = "마커 정보 목록")
        List<PropertyMarkerResponse> markers
) {

}
