package com.imjang.domain.auth.service;

import com.imjang.domain.auth.dto.request.EmailVerificationRequest;
import com.imjang.domain.auth.dto.request.ResendVerificationRequest;
import com.imjang.domain.auth.dto.request.SignUpRequest;
import com.imjang.domain.auth.dto.response.SignUpResponse;
import com.imjang.domain.auth.dto.response.VerificationResponse;
import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 통합 서비스
 * Controller에서 사용하는 Facade 역할
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final SignUpService signUpService;
  private final EmailVerificationService emailVerificationService;
  private final UserRepository userRepository;

  /**
   * 회원가입
   */
  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    return signUpService.signUp(request);
  }

  /**
   * 이메일 인증
   */
  @Transactional
  public VerificationResponse verifyEmail(EmailVerificationRequest request) {
    // 인증코드 검증
    boolean verified = emailVerificationService.verifyEmail(request.email(), request.code());

    if (!verified) {
      return VerificationResponse.failure("잘못된 인증코드이거나 만료되었습니다");
    }

    // 사용자 상태 업데이트
    User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.verifyEmail();

    log.info("Email verified for user: {}", request.email());
    return VerificationResponse.success();
  }

  /**
   * 인증코드 재발송
   */
  public void resendVerification(ResendVerificationRequest request) {
    // 사용자 존재 확인
    userRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 인증코드 재발송
    emailVerificationService.createAndSendVerification(request.email());
  }
}
