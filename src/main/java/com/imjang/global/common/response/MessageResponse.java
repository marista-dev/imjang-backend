package com.imjang.global.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메시지 응답 객체
 * 이 객체는 API 응답에서 단순한 메시지를 전달하기 위해 사용됩니다.
 */
@Schema(description = "메시지 응답")
public record MessageResponse(
        @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
        String message
) {

  public static MessageResponse of(String message) {
    return new MessageResponse(message);
  }
}
