package com.imjang.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
  // 성공 코드
  SUCCESS("S000", "성공"),
  CREATED("S001", "생성되었습니다"),
  UPDATED("S002", "수정되었습니다"),
  DELETED("S003", "삭제되었습니다");

  private final String code;
  private final String message;
}
