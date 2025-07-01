package com.imjang.domain.auth.service;

import com.imjang.domain.auth.entity.EmailVerification;
import com.imjang.domain.auth.repository.EmailVerificationRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 인증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

  private static final int CODE_LENGTH = 4;
  private static final int EXPIRY_MINUTES = 30;
  private static final int MAX_ATTEMPTS = 5;

  private final EmailVerificationRepository emailVerificationRepository;
  private final EmailService emailService;
  private final Random random = new Random();

  /**
   * 인증코드 생성 및 발송
   */
  @Transactional
  public void createAndSendVerification(String email) {
    // 재발송 제한 확인
    emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
            .ifPresent(verification -> {
              if (!verification.canResend()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                        "이메일 재발송은 1분 후에 가능합니다");
              }
            });

    // 인증코드 생성
    String code = generateCode();
    LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

    // 저장
    EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code(code)
            .expiresAt(expiresAt)
            .build();

    emailVerificationRepository.save(verification);

    // 이메일 발송
    emailService.sendVerificationEmail(email, code);

    log.info("Verification code created for: {}", email);
  }

  /**
   * 인증코드 검증
   */
  @Transactional
  public boolean verifyEmail(String email, String code) {
    // 최근 인증 정보 조회
    EmailVerification verification = emailVerificationRepository
            .findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                    "인증 정보를 찾을 수 없습니다"));

    // 시도 횟수 확인
    if (verification.getAttempts() >= MAX_ATTEMPTS) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
              "인증 시도 횟수를 초과했습니다");
    }

    verification.increaseAttempts();

    // 만료 확인
    if (verification.isExpired()) {
      return false;
    }

    // 코드 일치 확인
    if (!verification.getCode().equals(code)) {
      return false;
    }

    // 인증 완료
    verification.verify();
    return true;
  }

  private String generateCode() {
    return String.format("%04d", random.nextInt(10000));
  }
}
