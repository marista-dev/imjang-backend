package com.imjang.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 이메일 인증 응답 DTO
 */
@Schema(description = "이메일 인증 응답")
public record VerificationResponse(
        @Schema(description = "인증 성공 여부", example = "true")
        boolean verified,

        @Schema(description = "메시지", example = "이메일 인증이 완료되었습니다")
        String message
) {

  public static VerificationResponse success() {
    return new VerificationResponse(true, "이메일 인증이 완료되었습니다");
  }

  public static VerificationResponse failure(String message) {
    return new VerificationResponse(false, message);
  }
}
