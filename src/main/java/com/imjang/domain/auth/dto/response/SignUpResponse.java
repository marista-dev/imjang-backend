package com.imjang.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답 DTO")
public record SignUpResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "응답 메시지", example = "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
        String message
) {

  public static SignUpResponse of(Long userId, String email) {
    return new SignUpResponse(
            userId,
            email,
            "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요."
    );
  }
}
