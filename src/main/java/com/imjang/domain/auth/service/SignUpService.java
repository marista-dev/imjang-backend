package com.imjang.domain.auth.service;

import com.imjang.domain.auth.dto.request.SignUpRequest;
import com.imjang.domain.auth.dto.response.SignUpResponse;
import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailVerificationService emailVerificationService;

  /**
   * 회원가입 처리
   *
   * @param request
   *         회원가입 요청 정보
   * @return 회원가입 응답
   */
  public SignUpResponse signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new CustomException(ErrorCode.DUPLICATE_USER);
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = User.builder()
            .email(request.email())
            .password(encodedPassword)
            .build();

    User savedUser = userRepository.save(user);

    // 이메일 인증 코드 발송
    try {
      emailVerificationService.createAndSendVerification(savedUser.getEmail());
    } catch (Exception e) {
      log.error("인증 이메일 보내기에 실패했습니다: {}", savedUser.getEmail(), e);
      // 이메일 발송 실패해도 회원가입은 성공 처리
    }

    log.info("새로운 가입자: {}", savedUser.getEmail());

    return SignUpResponse.of(savedUser.getId(), savedUser.getEmail());
  }
}
