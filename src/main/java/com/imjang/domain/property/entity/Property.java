package com.imjang.domain.property.entity;

import com.imjang.domain.auth.entity.User;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "properties",
        indexes = {
                @Index(name = "idx_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_location", columnList = "latitude, longitude"),
                @Index(name = "idx_property_h3", columnList = "h3_index")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Property extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 위치정보
  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  // H3 인덱스
  @Column(name = "h3_index", length = 15)
  private String h3Index;

  // 위치 정보 조회 상태
  @Enumerated(EnumType.STRING)
  @Column(name = "location_fetch_status", length = 20)
  @Builder.Default
  private LocationFetchStatus locationFetchStatus = LocationFetchStatus.PENDING;

  @Column(name = "location_fetched_at")
  private LocalDateTime locationFetchedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "price_type", nullable = false, length = 20)
  private PropertyType priceType;

  // 가격정보
  private Long deposit;

  @Column(name = "monthly_rent")
  private Long monthlyRent;

  private Long price;

  @Column(name = "maintenance_fee")
  private Long maintenanceFee;

  // 매물 정보
  @Column(nullable = false)
  private Integer area;

  @Column(name = "current_floor", nullable = false)
  private Integer currentFloor;

  @Column(name = "total_floor", nullable = false)
  private Integer totalFloor;

  // 사용자 평가 정보
  @Column(nullable = false)
  private Integer rating;

  @Column(name = "move_in_available", nullable = false)
  private boolean moveInAvailable;

  @Column(name = "revisit_intention", nullable = false)
  private boolean revisitIntention;

  @Enumerated(EnumType.STRING)
  @Column(name = "price_evaluation", nullable = false, length = 20)
  private PriceEvaluation priceEvaluation;

  @Column(columnDefinition = "TEXT")
  private String memo;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  // 위치 정보 조회 상태 업데이트
  public void updateLocationFetchStatus(LocationFetchStatus status) {
    this.locationFetchStatus = status;
    if (status == LocationFetchStatus.COMPLETED) {
      this.locationFetchedAt = LocalDateTime.now();
    }
  }

  public void updateH3Index(String h3Index) {
    this.h3Index = h3Index;
  }
}
