package com.imjang.domain.auth.dto.response;

import com.imjang.domain.auth.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답 DTO
 */
@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 권한", example = "USER")
        UserRole role,

        @Schema(description = "메시지", example = "로그인 성공")
        String message
) {

  public static LoginResponse of(Long userId, String email, UserRole role) {
    return new LoginResponse(userId, email, role, "로그인 성공");
  }
}
