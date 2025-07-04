package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyType {
  MONTHLY("월세"),
  JEONSE("전세"),
  SALE("매매");

  private final String description;
}
