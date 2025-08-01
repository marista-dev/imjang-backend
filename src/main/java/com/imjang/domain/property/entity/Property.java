package com.imjang.domain.property.entity;

import com.imjang.domain.auth.entity.User;
import com.imjang.global.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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

  // 주차 가능 여부
  @Enumerated(EnumType.STRING)
  @Column(name = "parking_type", length = 20)
  @Builder.Default
  private ParkingType parkingType = ParkingType.UNKNOWN;

  // 주변 환경 정보
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
          name = "property_environments",
          joinColumns = @JoinColumn(name = "property_id")
  )
  @Column(name = "environment_type")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Set<EnvironmentType> environments = new HashSet<>();

  @Column(columnDefinition = "TEXT")
  private String memo;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  // 상세 정보 업데이트
  public void updateDetails(Boolean moveInAvailable,
                            Boolean revisitIntention,
                            PriceEvaluation priceEvaluation,
                            ParkingType parkingType,
                            Long maintenanceFee,
                            Set<EnvironmentType> environments,
                            String memo) {

    if (moveInAvailable != null) {
      this.moveInAvailable = moveInAvailable.booleanValue();
    }

    if (revisitIntention != null) {
      this.revisitIntention = revisitIntention.booleanValue();
    }

    if (priceEvaluation != null) {
      this.priceEvaluation = priceEvaluation;
    }

    if (parkingType != null) {
      this.parkingType = parkingType;
    }

    if (maintenanceFee != null) {
      this.maintenanceFee = maintenanceFee;
    }

    if (environments != null) {
      this.environments.clear();
      this.environments.addAll(environments);
    }

    if (memo != null) {
      this.memo = memo;
    }
  }
}
