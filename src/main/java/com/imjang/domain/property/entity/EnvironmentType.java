package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvironmentType {
  // 분위기
  QUIET("조용함"),
  BUSY_AREA("번화가"),
  RESIDENTIAL("주택가"),
  COMMERCIAL("상업지구"),

  // 자연환경
  NEAR_PARK("공원인접"),
  NEAR_MOUNTAIN("산인접"),
  NEAR_RIVER("하천인접"),
  GREEN_LACK("녹지부족"),

  // 소음
  MAIN_ROAD("대로변"),
  ALLEY("골목안"),
  UNDER_CONSTRUCTION("공사중"),
  NEAR_SCHOOL("학교인근");

  private final String description;
}
