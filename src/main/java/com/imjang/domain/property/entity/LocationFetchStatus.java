package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationFetchStatus {
  PENDING("대기중"),
  IN_PROGRESS("진행중"),
  COMPLETED("완료"),
  FAILED("실패");

  private final String description;
}
