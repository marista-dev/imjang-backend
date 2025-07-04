package com.imjang.domain.property.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageUploadResponse(
        @Schema(description = "이미지 ID", example = "1234")
        Long imageId,

        @Schema(description = "썸네일 URL", example = "https://imjang-bucket.s3.ap-northeast-2.amazonaws"
                + ".com/temp/2024/01/15/uuid-thumb.jpg")
        String thumbnailUrl,

        @Schema(description = "업로드 상태", example = "COMPLETED")
        String status
) {

  public static ImageUploadResponse of(Long imageId, String thumbnailUrl) {
    return new ImageUploadResponse(imageId, thumbnailUrl, "COMPLETED");
  }
}
