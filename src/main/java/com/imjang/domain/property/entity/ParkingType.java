package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParkingType {
  AVAILABLE("가능"),
  NOT_AVAILABLE("불가"),
  CONDITIONAL("조건부"),
  UNKNOWN("미확인");

  private final String description;
}
