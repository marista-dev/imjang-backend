package com.imjang.domain.property.location.entity;

import com.imjang.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "location_cache",
        indexes = {
                @Index(name = "idx_h3_index", columnList = "h3_index", unique = true),
                @Index(name = "idx_last_fetched", columnList = "last_fetched_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LocationCache extends BaseEntity {

  @Column(name = "h3_index", nullable = false, unique = true, length = 15)
  private String h3Index;

  @Column(name = "center_lat", nullable = false)
  private Double centerLat;

  @Column(name = "center_lng", nullable = false)
  private Double centerLng;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "transit_data", columnDefinition = "jsonb")
  @Builder.Default
  private String transitData = "{}";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "amenities_data", columnDefinition = "jsonb")
  @Builder.Default
  private String amenitiesData = "{}";

  @Column(name = "search_radius", nullable = false)
  @Builder.Default
  private Integer searchRadius = 1000;

  @Column(name = "source", nullable = false, length = 50)
  @Builder.Default
  private String source = "KAKAO";

  @Column(name = "api_call_count")
  @Builder.Default
  private Integer apiCallCount = 0;

  @Column(name = "last_fetched_at", nullable = false)
  private LocalDateTime lastFetchedAt;

  public void updateData(String transitData, String amenitiesData) {
    this.transitData = transitData;
    this.amenitiesData = amenitiesData;
    this.lastFetchedAt = LocalDateTime.now();
    this.apiCallCount++;
  }
}
