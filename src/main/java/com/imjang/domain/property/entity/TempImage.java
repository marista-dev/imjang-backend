package com.imjang.domain.property.entity;

import com.imjang.domain.auth.entity.User;
import com.imjang.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "temp_images",
        indexes = {
                @Index(name = "idx_user_expires", columnList = "user_id, expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TempImage extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "original_url", nullable = false, length = 500)
  private String originalUrl;

  @Column(name = "thumbnail_url", nullable = false, length = 500)
  private String thumbnailUrl;

  @Column(name = "expires_at", nullable = false)
  @Builder.Default
  private LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

}
