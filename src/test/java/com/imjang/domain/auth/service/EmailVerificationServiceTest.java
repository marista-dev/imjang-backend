package com.imjang.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.imjang.domain.auth.entity.EmailVerification;
import com.imjang.domain.auth.repository.EmailVerificationRepository;
import com.imjang.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

  @InjectMocks
  private EmailVerificationService emailVerificationService;

  @Mock
  private EmailVerificationRepository emailVerificationRepository;

  @Mock
  private EmailService emailService;

  @Test
  @DisplayName("4자리 인증코드 생성 및 저장")
  void createVerification_Success() {
    // given
    String email = "test@example.com";
    given(emailVerificationRepository.save(any(EmailVerification.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

    // when
    emailVerificationService.createAndSendVerification(email);

    // then
    verify(emailVerificationRepository).save(any(EmailVerification.class));
    verify(emailService).sendVerificationEmail(anyString(), anyString());
  }

  @Test
  @DisplayName("재발송 시간 제한 확인")
  void resendVerification_TooSoon_ThrowsException() {
    // given
    String email = "test@example.com";
    EmailVerification recentVerification = EmailVerification.builder()
            .email(email)
            .code("1234")
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();

    given(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email))
            .willReturn(Optional.of(recentVerification));

    // when & then
    assertThatThrownBy(() -> emailVerificationService.createAndSendVerification(email))
            .isInstanceOf(CustomException.class)
            .hasMessage("이메일 재발송은 1분 후에 가능합니다");
  }

  @Test
  @DisplayName("올바른 인증코드 검증 성공")
  void verifyEmail_Success() {
    // given
    String email = "test@example.com";
    String code = "1234";
    EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code(code)
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();

    given(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email))
            .willReturn(Optional.of(verification));

    // when
    boolean result = emailVerificationService.verifyEmail(email, code);

    // then
    assertThat(result).isTrue();
    assertThat(verification.isVerified()).isTrue();
  }

  @Test
  @DisplayName("만료된 인증코드 검증 실패")
  void verifyEmail_Expired_ReturnsFalse() {
    // given
    String email = "test@example.com";
    String code = "1234";
    EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code(code)
            .expiresAt(LocalDateTime.now().minusMinutes(1))
            .build();

    given(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email))
            .willReturn(Optional.of(verification));

    // when
    boolean result = emailVerificationService.verifyEmail(email, code);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("5회 초과 시도 차단")
  void verifyEmail_TooManyAttempts_ThrowsException() {
    // given
    String email = "test@example.com";
    String code = "9999";
    EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code("1234")
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();

    // 이미 5회 시도
    for (int i = 0; i < 5; i++) {
      verification.increaseAttempts();
    }

    given(emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email))
            .willReturn(Optional.of(verification));

    // when & then
    assertThatThrownBy(() -> emailVerificationService.verifyEmail(email, code))
            .isInstanceOf(CustomException.class)
            .hasMessage("인증 시도 횟수를 초과했습니다");
  }
}
