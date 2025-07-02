package com.imjang.domain.auth.service;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.auth.dto.request.LoginRequest;
import com.imjang.domain.auth.dto.response.LoginResponse;
import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.entity.UserStatus;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

  public static final String SESSION_KEY = "USER_SESSION";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 로그인 처리
   *
   * @param request
   *         로그인 요청 DTO
   */
  public LoginResponse login(LoginRequest request, HttpSession session) {
    User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new CustomException(ErrorCode.UNAUTHORIZED, "이메일 인증이 필요합니다.");
    }

    UserSession userSession = UserSession.of(
            user.getId(),
            user.getEmail(),
            user.getRole()
    );

    session.setAttribute(SESSION_KEY, userSession);

    log.info("유저가 로그인했습니다. email: {}", user.getEmail());

    return LoginResponse.of(user.getId(), user.getEmail(), user.getRole());
  }

  /**
   * 로그아웃 처리
   */
  public void logout(HttpSession session) {
    if (session != null) {
      UserSession userSession = (UserSession) session.getAttribute(SESSION_KEY);
      if (userSession != null) {
        log.info("User logged out: {}", userSession.email());
      }
      session.invalidate();
    }
  }

  /**
   * 현재 로그인한 사용자 정보 조회
   */
  public UserSession getCurrentUser(HttpSession session) {
    if (session == null) {
      return null;
    }
    return (UserSession) session.getAttribute(SESSION_KEY);
  }
}
