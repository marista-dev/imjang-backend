package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.auth.service.LoginService;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.dto.request.PrefetchLocationRequest;
import com.imjang.domain.property.dto.response.PropertyDetailResponse;
import com.imjang.domain.property.event.LocationPrefetchEvent;
import com.imjang.domain.property.service.PropertyDetailService;
import com.imjang.domain.property.service.PropertyService;
import com.imjang.global.annotation.LoginRequired;
import com.imjang.global.common.response.MessageResponse;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final PropertyDetailService propertyDetailService;
  private final ApplicationEventPublisher eventPublisher;

  @Operation(summary = "위치 정보 사전 수집",
          description = "매물 입력 전 위치 정보를 미리 수집합니다. 클라이언트에서 좌표를 받으면 즉시 호출해주세요.")
  @PostMapping("/location/prefetch")
  @LoginRequired
  public ResponseEntity<MessageResponse> prefetchLocation(
          @Valid @RequestBody PrefetchLocationRequest request,
          HttpSession session) {

    UserSession userSession = (UserSession) session.getAttribute(LoginService.SESSION_KEY);
    if (userSession == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 비동기로 위치 정보 수집 시작
    eventPublisher.publishEvent(new LocationPrefetchEvent(
            request.latitude(),
            request.longitude(),
            request.address(),
            userSession.userId()
    ));

    return ResponseEntity.ok(
            MessageResponse.of("위치 정보 수집이 시작되었습니다.")
    );
  }

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

  @Operation(summary = "매물 상세 조회",
          description = "매물의 상세 정보를 조회합니다. 위치 정보가 아직 수집되지 않았다면 locationInfo는 null로 반환됩니다.")
  @GetMapping("/{propertyId}/detail")
  @LoginRequired
  public ResponseEntity<PropertyDetailResponse> getPropertyDetail(
          @Parameter(description = "매물 ID", required = true, example = "1")
          @PathVariable Long propertyId,
          HttpSession session) {

    UserSession userSession = (UserSession) session.getAttribute(LoginService.SESSION_KEY);
    if (userSession == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    PropertyDetailResponse response = propertyDetailService.getPropertyDetail(propertyId, userSession.userId());

    return ResponseEntity.ok(response);
  }
}
