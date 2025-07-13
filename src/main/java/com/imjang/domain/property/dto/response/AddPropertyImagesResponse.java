package com.imjang.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매물 이미지 추가 응답")
public record AddPropertyImagesResponse(
        @Schema(description = "추가된 이미지 ID 목록", example = "[5678, 5679, 5680]")
        List<Long> imageIds
) {

  public static AddPropertyImagesResponse of(List<Long> imageIds) {
    return new AddPropertyImagesResponse(imageIds);
  }
}
