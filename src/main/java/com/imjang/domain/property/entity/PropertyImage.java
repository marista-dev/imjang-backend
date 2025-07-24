package com.imjang.domain.property.entity;

import com.imjang.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "property_images",
        indexes = {
                @Index(name = "idx_property_id", columnList = "property_id"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PropertyImage extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  @Column(name = "temp_image_id")
  private Long tempImageId;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private ImageStatus status = ImageStatus.PENDING;

  /**
   * S3 업로드 완료 후 URL 업데이트
   */
  public void updateUrls(String imageUrl, String thumbnailUrl) {
    this.imageUrl = imageUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.status = ImageStatus.COMPLETED;
  }

  /**
   * 상태 변경
   */
  public void updateStatus(ImageStatus status) {
    this.status = status;
  }
}
