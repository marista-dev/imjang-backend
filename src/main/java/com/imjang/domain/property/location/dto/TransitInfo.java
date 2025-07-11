package com.imjang.domain.property.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransitInfo(
        @JsonProperty("nearestSubwayStation") String nearestSubwayStation,
        @JsonProperty("subwayDistance") Integer subwayDistance,        // 미터 단위
        @JsonProperty("subwayWalkTime") Integer subwayWalkTime,        // 분 단위
        @JsonProperty("busStopCount") Integer busStopCount             // 500m 내 버스정류장 수
) {

  public static TransitInfo empty() {
    return new TransitInfo(null, null, null, 0);
  }
}
