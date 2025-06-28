package com.imjang.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imjang.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private String code;
  private String message;
  private LocalDateTime timestamp;
  private String path;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ValidationError> errors = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String trace;

  @Builder
  private ErrorResponse(String code, String message, String path,
                        List<ValidationError> errors, String trace) {
    this.code = code;
    this.message = message;
    this.timestamp = LocalDateTime.now();
    this.path = path;
    this.errors = errors != null ? errors : new ArrayList<>();
    this.trace = trace;
  }

  // ErrorCode로부터 생성
  public static ErrorResponse of(ErrorCode errorCode, String path) {
    return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .path(path)
            .build();
  }

  // 커스텀 메시지와 함께 생성
  public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
    return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(message)
            .path(path)
            .build();
  }

  // Validation 에러와 함께 생성
  public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> fieldErrors) {
    List<ValidationError> validationErrors = fieldErrors.stream()
            .map(ValidationError::of)
            .toList();

    return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .path(path)
            .errors(validationErrors)
            .build();
  }

  // 스택트레이스 추가 (개발환경용)
  public ErrorResponse withTrace(String trace) {
    this.trace = trace;
    return this;
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class ValidationError {

    private String field;
    private Object value;
    private String reason;

    @Builder
    private ValidationError(String field, Object value, String reason) {
      this.field = field;
      this.value = value;
      this.reason = reason;
    }

    public static ValidationError of(FieldError fieldError) {
      return ValidationError.builder()
              .field(fieldError.getField())
              .value(fieldError.getRejectedValue())
              .reason(fieldError.getDefaultMessage())
              .build();
    }
  }
}
