package com.imjang.infrastructure.kakao.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KakaoCategoryCode {

  MART("MT1", "대형마트"),
  CONVENIENCE_STORE("CS2", "편의점"),
  SUBWAY("SW8", "지하철역"),
  BANK("BK9", "은행"),
  HOSPITAL("HP8", "병원"),
  PHARMACY("PM9", "약국");

  private final String code;
  private final String description;
}
