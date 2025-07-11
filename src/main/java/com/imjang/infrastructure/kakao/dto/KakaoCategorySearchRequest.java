package com.imjang.infrastructure.kakao.dto;

import lombok.Builder;

@Builder
public record KakaoCategorySearchRequest(
        String categoryGroupCode,  // 카테고리 그룹 코드 (MT1, CS2, SW8 등)
        String x,                  // 중심 좌표 경도
        String y,                  // 중심 좌표 위도
        Integer radius,            // 검색 반경 (미터 단위, 최대 20000)
        Integer page,              // 페이지 번호 (1~45)
        Integer size,              // 한 페이지 문서 수 (1~15)
        String sort                // 정렬 기준 (distance, accuracy)
) {

  public static KakaoCategorySearchRequest of(String categoryGroupCode, Double lng, Double lat, int radius) {
    return KakaoCategorySearchRequest.builder()
            .categoryGroupCode(categoryGroupCode)
            .x(String.valueOf(lng))
            .y(String.valueOf(lat))
            .radius(radius)
            .page(1)
            .size(15)
            .sort("distance")
            .build();
  }
}
