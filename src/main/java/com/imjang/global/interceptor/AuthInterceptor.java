package com.imjang.global.interceptor;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.auth.service.LoginService;
import com.imjang.global.annotation.LoginRequired;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 인증 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    HandlerMethod handlerMethod = (HandlerMethod) handler;

    LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
    if (loginRequired == null) {
      loginRequired = handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
    }
    if (loginRequired == null) {
      // 로그인 필요하지 않은 경우
      return true;
    }
    HttpSession session = request.getSession(false);
    if (session == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    UserSession userSession = (UserSession) session.getAttribute(LoginService.SESSION_KEY);
    if (userSession == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 검증된 사용자 세션을 request attribute에 저장
    request.setAttribute("USER_SESSION", userSession);

    log.debug("🔓인증된 사용자 정보: {}", userSession.email());

    return true;
  }
}
