package com.imjang.domain.auth.dto;

import com.imjang.domain.auth.entity.UserRole;
import java.io.Serializable;

/**
 * 세션 저장될 사용자 정보
 * TODO: Serializable 구현 필요
 */
public record UserSession(
        Long userId,
        String email,
        UserRole role
) implements Serializable {

  public static UserSession of(Long userId, String email, UserRole role) {
    return new UserSession(userId, email, role);
  }

  public boolean isAdmin() {
    return role == UserRole.ADMIN;
  }
}
