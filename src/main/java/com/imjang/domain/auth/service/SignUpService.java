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

    log.info("회원가입 성공: {}", savedUser.getEmail());
    return SignUpResponse.of(savedUser.getId(), savedUser.getEmail());
  }
}
