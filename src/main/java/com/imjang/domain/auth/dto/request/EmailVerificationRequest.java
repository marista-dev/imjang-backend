package com.imjang.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 이메일 인증 요청 DTO
 */
@Schema(description = "이메일 인증 요청")
public record EmailVerificationRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "인증코드", example = "1234")
        @NotBlank(message = "인증코드는 필수입니다")
        @Pattern(regexp = "^\\d{4}$", message = "인증코드는 4자리 숫자입니다")
        String code
) {

}
