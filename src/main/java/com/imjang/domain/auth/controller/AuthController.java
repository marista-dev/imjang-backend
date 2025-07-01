package com.imjang.domain.auth.controller;

import com.imjang.domain.auth.dto.request.EmailVerificationRequest;
import com.imjang.domain.auth.dto.request.ResendVerificationRequest;
import com.imjang.domain.auth.dto.request.SignUpRequest;
import com.imjang.domain.auth.dto.response.SignUpResponse;
import com.imjang.domain.auth.dto.response.VerificationResponse;
import com.imjang.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API
 */
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * 회원가입
   * POST /api/v1/auth/registrations
   */
  @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입을 진행합니다.")
  @PostMapping("/registrations")
  public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
    SignUpResponse response = authService.signUp(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 이메일 인증
   * POST /api/v1/auth/verifications
   */
  @Operation(summary = "이메일 인증", description = "이메일로 받은 인증코드를 검증합니다.")
  @PostMapping("/verifications")
  public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
    VerificationResponse response = authService.verifyEmail(request);
    return ResponseEntity.ok(response);
  }

  /**
   * 인증코드 재발송
   * POST /api/v1/auth/verifications:resend
   */
  @Operation(summary = "인증코드 재발송", description = "이메일 인증코드를 재발송합니다.")
  @PostMapping("/verifications:resend")
  public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
    authService.resendVerification(request);
    return ResponseEntity.noContent().build();
  }
}
