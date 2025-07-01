package com.imjang.domain.auth.entity;

import com.imjang.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

  @Column(nullable = false)
  private String email;

  @Column(nullable = false, length = 4)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private int attempts = 0;

  @Column(nullable = false)
  private boolean verified = false;

  @Column(name = "verified_at")
  private LocalDateTime verifiedAt;

  @Column(name = "last_sent_at", nullable = false)
  private LocalDateTime lastSentAt;

  @Builder
  public EmailVerification(String email, String code, LocalDateTime expiresAt) {
    this.email = email;
    this.code = code;
    this.expiresAt = expiresAt;
    this.lastSentAt = LocalDateTime.now();
  }

  public void verify() {
    this.verified = true;
    this.verifiedAt = LocalDateTime.now();
  }

  public void increaseAttempts() {
    this.attempts++;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiresAt);
  }

  public boolean canResend() {
    return LocalDateTime.now().isAfter(this.lastSentAt.plusMinutes(1));
  }

  public void updateForResend(String newCode, LocalDateTime newExpiresAt) {
    this.code = newCode;
    this.expiresAt = newExpiresAt;
    this.lastSentAt = LocalDateTime.now();
    this.attempts = 0;
  }
}
