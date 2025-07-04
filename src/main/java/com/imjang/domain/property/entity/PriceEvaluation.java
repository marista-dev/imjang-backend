package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PriceEvaluation {
  REASONABLE("적정가"),
  EXPENSIVE("비쌈"),
  CHEAP("저렴");

  private final String description;
}
