package com.imjang.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageStatus {
  PENDING("대기중"),
  UPLOADING("업로드 중"),
  COMPLETED("업로드 완료"),
  FAILED("업로드 실패"),
  DELETED("삭제됨");

  private final String description;
}
