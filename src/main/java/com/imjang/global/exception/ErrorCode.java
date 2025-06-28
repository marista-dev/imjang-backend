package com.imjang.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다"),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다"),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "잘못된 타입입니다"),
  HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근이 거부되었습니다"),

  // Business
  PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "매물을 찾을 수 없습니다"),
  DUPLICATE_PROPERTY(HttpStatus.CONFLICT, "P002", "이미 등록된 매물입니다"),
  INVALID_PROPERTY_STATUS(HttpStatus.BAD_REQUEST, "P003", "잘못된 매물 상태입니다"),

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
  DUPLICATE_USER(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다"),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "잘못된 비밀번호입니다"),

  // Auth
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다"),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다"),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다"),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "권한이 없습니다"),

  // File
  FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "F001", "파일 업로드에 실패했습니다"),
  INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F002", "지원하지 않는 파일 형식입니다"),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F003", "파일 크기가 너무 큽니다"),

  // Database
  DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "D001", "데이터 무결성 위반입니다"),
  CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "D002", "제약 조건 위반입니다");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
  
}
