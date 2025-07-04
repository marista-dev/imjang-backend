package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.auth.service.LoginService;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.service.PropertyService;
import com.imjang.global.annotation.LoginRequired;
import com.imjang.global.common.response.MessageResponse;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Property", description = "매물 관련 API")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

  private final PropertyService propertyService;

  @Operation(summary = "매물 빠른 기록", description = "임장 중 매물 정보를 빠르게 기록합니다.")
  @PostMapping
  @LoginRequired
  public ResponseEntity<MessageResponse> createProperty(
          @Valid @RequestBody CreatePropertyRequest request,
          HttpSession session) {

    UserSession userSession = (UserSession) session.getAttribute(LoginService.SESSION_KEY);
    if (userSession == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    propertyService.createProperty(request, userSession.userId());

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(MessageResponse.of("매물이 성공적으로 기록되었습니다."));
  }
}
