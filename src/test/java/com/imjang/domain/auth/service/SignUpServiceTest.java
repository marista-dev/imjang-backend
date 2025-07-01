package com.imjang.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.imjang.domain.auth.dto.request.SignUpRequest;
import com.imjang.domain.auth.dto.response.SignUpResponse;
import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

  @InjectMocks
  private SignUpService signUpService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("정상적인 회원가입 성공")
  void signUp_Success() throws Exception {
    //given
    SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "Password123!"
    );

    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      // 실제 DB 저장 시뮬레이션 - ID 할당
      return User.builder()
              .email(user.getEmail())
              .password(user.getPassword())
              .build();
    });

    // when
    SignUpResponse response = signUpService.signUp(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.message()).contains("회원가입이 완료되었습니다");

    verify(userRepository).existsByEmail(request.email());
    verify(passwordEncoder).encode(request.password());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("이메일 중복 시 회원가입 실패")
  void signUp_DuplicateEmail_ThrowException() throws Exception {
    //given
    SignUpRequest request = new SignUpRequest(
            "duplicate@example.com",
            "Password123!"
    );
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    //when & then
    assertThatThrownBy(() -> signUpService.signUp(request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USER);

    verify(userRepository).existsByEmail(request.email());
    verify(passwordEncoder, never()).encode(request.password());
    verify(userRepository, never()).save(any(User.class));
  }


}
