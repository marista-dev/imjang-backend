package com.imjang.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imjang.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse<T> {

  private int status;
  private String code;
  private String message;
  private T data;
  private LocalDateTime timestamp;

  @Builder
  private RestResponse(int status, String code, String message, T data) {
    this.status = status;
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  // 성공 응답 - 데이터만
  public static <T> RestResponse<T> success(T data) {
    return RestResponse.<T>builder()
            .status(HttpStatus.OK.value())
            .code(ResultCode.SUCCESS.getCode())
            .message(ResultCode.SUCCESS.getMessage())
            .data(data)
            .build();
  }

  // 성공 응답 - 메시지만
  public static RestResponse<Void> success(String message) {
    return RestResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .code(ResultCode.SUCCESS.getCode())
            .message(message)
            .build();
  }

  // 성공 응답 - 데이터 + 커스텀 메시지
  public static <T> RestResponse<T> success(T data, String message) {
    return RestResponse.<T>builder()
            .status(HttpStatus.OK.value())
            .code(ResultCode.SUCCESS.getCode())
            .message(message)
            .data(data)
            .build();
  }

  // 실패 응답
  public static RestResponse<Void> error(ErrorCode errorCode) {
    return RestResponse.<Void>builder()
            .status(errorCode.getHttpStatus().value())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build();
  }

  // 실패 응답 - 커스텀 메시지
  public static RestResponse<Void> error(ErrorCode errorCode, String message) {
    return RestResponse.<Void>builder()
            .status(errorCode.getHttpStatus().value())
            .code(errorCode.getCode())
            .message(message)
            .build();
  }
}
