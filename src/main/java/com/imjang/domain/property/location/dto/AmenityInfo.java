package com.imjang.domain.property.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AmenityInfo(
        @JsonProperty("category") String category,                      // 카테고리명 (편의점, 병원 등)
        @JsonProperty("categoryCode") String categoryCode,              // 카테고리 코드
        @JsonProperty("count") Integer count,                           // 검색 반경 내 개수
        @JsonProperty("nearestName") String nearestName,                // 가장 가까운 시설명
        @JsonProperty("nearestDistance") Integer nearestDistance        // 가장 가까운 시설까지 거리 (미터)
) {

}
