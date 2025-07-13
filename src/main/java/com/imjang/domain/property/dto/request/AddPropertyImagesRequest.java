package com.imjang.domain.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "매물 이미지 추가 요청")
public record AddPropertyImagesRequest(
        @Schema(description = "임시 이미지 ID 목록", example = "[1234, 5678, 9012]")
        @NotNull(message = "이미지 ID 목록은 필수입니다")
        @Size(min = 1, message = "최소 1개 이상의 이미지가 필요합니다")
        List<Long> tempImageIds
) {

}
