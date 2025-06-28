package com.imjang.global.exception;


import com.imjang.global.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


  /**
   * 커스텀 비즈니스 예외 처리
   */
  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(
          CustomException e, HttpServletRequest request) {

    log.error("CustomException: {}", e.getMessage(), e);

    ErrorResponse response = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
  }

  /**
   * @Valid 검증 실패 예외 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
          MethodArgumentNotValidException e, HttpServletRequest request) {

    log.error("Validation failed: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI(),
            e.getBindingResult().getFieldErrors()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * @ModelAttribute 바인딩 실패 예외 처리
   */
  @ExceptionHandler(BindException.class)
  protected ResponseEntity<ErrorResponse> handleBindException(
          BindException e, HttpServletRequest request) {

    log.error("Binding failed: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI(),
            e.getBindingResult().getFieldErrors()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * 타입 불일치 예외 처리
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
          MethodArgumentTypeMismatchException e, HttpServletRequest request) {

    log.error("Type mismatch: {}", e.getMessage());

    String message = String.format("'%s' 파라미터의 값 '%s'이(가) 잘못되었습니다",
            e.getName(), e.getValue());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_TYPE_VALUE,
            message,
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * HTTP 메시지 파싱 실패
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
          HttpMessageNotReadableException e, HttpServletRequest request) {

    log.error("Message not readable: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            "요청 본문을 읽을 수 없습니다",
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * 필수 파라미터 누락
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
          MissingServletRequestParameterException e, HttpServletRequest request) {

    log.error("Missing parameter: {}", e.getMessage());

    String message = String.format("필수 파라미터 '%s'이(가) 누락되었습니다", e.getParameterName());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            message,
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * 지원하지 않는 HTTP 메서드
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
          HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

    log.error("Method not allowed: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.METHOD_NOT_ALLOWED,
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
  }

  /**
   * 404 Not Found
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
          NoHandlerFoundException e, HttpServletRequest request) {

    log.error("No handler found: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.ENTITY_NOT_FOUND,
            "요청한 리소스를 찾을 수 없습니다",
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * 데이터베이스 무결성 위반
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
          DataIntegrityViolationException e, HttpServletRequest request) {

    log.error("Data integrity violation: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.DATA_INTEGRITY_VIOLATION,
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }
//
//  /**
//   * 인증 실패
//   */
//  @ExceptionHandler(AuthenticationException.class)
//  protected ResponseEntity<ErrorResponse> handleAuthenticationException(
//          AuthenticationException e, HttpServletRequest request) {
//
//    log.error("Authentication failed: {}", e.getMessage());
//
//    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
//    if (e instanceof BadCredentialsException) {
//      errorCode = ErrorCode.INVALID_PASSWORD;
//    }
//
//    ErrorResponse response = ErrorResponse.of(errorCode, request.getRequestURI());
//    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//  }
//
//  /**
//   * 인가 실패
//   */
//  @ExceptionHandler(AccessDeniedException.class)
//  protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
//          AccessDeniedException e, HttpServletRequest request) {
//
//    log.error("Access denied: {}", e.getMessage());
//
//    ErrorResponse response = ErrorResponse.of(
//            ErrorCode.ACCESS_DENIED,
//            request.getRequestURI()
//    );
//
//    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//  }

  /**
   * 기타 모든 예외 처리
   */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(
          Exception e, HttpServletRequest request) {

    log.error("Unexpected error: {}", e.getMessage(), e);

    ErrorResponse response = ErrorResponse.of(
            ErrorCode.INTERNAL_SERVER_ERROR,
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
