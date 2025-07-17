package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarkerColor {
  GREEN("만족"),
  YELLOW("보통"),
  RED("불만족");

  private final String description;
}
