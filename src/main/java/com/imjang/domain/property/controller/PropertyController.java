package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.dto.request.PrefetchLocationRequest;
import com.imjang.domain.property.dto.request.UpdatePropertyDetailRequest;
import com.imjang.domain.property.dto.response.PropertyDetailResponse;
import com.imjang.domain.property.dto.response.RecentPropertyResponse;
import com.imjang.domain.property.dto.response.UpdatePropertyDetailResponse;
import com.imjang.domain.property.event.LocationPrefetchEvent;
import com.imjang.domain.property.service.PropertyDetailService;
import com.imjang.domain.property.service.PropertyService;
import com.imjang.global.annotation.LoginRequired;
import com.imjang.global.common.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

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

  @Operation(summary = "매물 빠른 기록", description = "임장 중 매물 정보를 빠르게 기록")
  @PostMapping
  @LoginRequired
  public ResponseEntity<MessageResponse> createProperty(
          @Valid @RequestBody CreatePropertyRequest request,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");
    propertyService.createProperty(request, userSession.userId());

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(MessageResponse.of("매물이 성공적으로 기록되었습니다."));
  }

  @Operation(summary = "매물 상세 조회",
          description = "매물의 상세 정보를 조회 위치 정보가 아직 수집되지 않았다면 locationInfo는 null로 반환")
  @GetMapping("/{propertyId}/detail")
  @LoginRequired
  public ResponseEntity<PropertyDetailResponse> getPropertyDetail(
          @Parameter(description = "매물 ID", required = true, example = "1")
          @PathVariable Long propertyId,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

    PropertyDetailResponse response = propertyDetailService.getPropertyDetail(propertyId, userSession.userId());

    return ResponseEntity.ok(response);
  }

  // 기존 메서드들 아래에 추가할 PATCH 엔드포인트
  @Operation(summary = "매물 상세 정보 수정",
          description = "매물의 체크리스트 및 추가 정보를 수정. 전달된 필드만 업데이트")
  @PatchMapping("/{propertyId}/detail")
  @LoginRequired
  public ResponseEntity<UpdatePropertyDetailResponse> updatePropertyDetail(
          @Parameter(description = "매물 ID", required = true, example = "123")
          @PathVariable Long propertyId,
          @Valid @RequestBody UpdatePropertyDetailRequest request,
          HttpServletRequest servletRequest) {
    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

    UpdatePropertyDetailResponse response = propertyDetailService.updatePropertyDetail(
            propertyId,
            request,
            userSession.userId()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "최근 매물 목록 조회",
          description = "메인 페이지에 표시할 최근 매물 목록을 조회합니다. 기본 3개, 최대 10개까지 조회 가능")
  @GetMapping("/recent")
  @LoginRequired
  public ResponseEntity<RecentPropertyResponse> getRecentProperties(
          @Parameter(description = "조회할 매물 개수", example = "3")
          @RequestParam(value = "limit", defaultValue = "3")
          @Min(value = 1, message = "최소 1개 이상")
          @Max(value = 10, message = "최대 10개까지 조회 가능")
          Integer limit,
          HttpServletRequest servletRequest) {
    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

    RecentPropertyResponse response = propertyService.getRecentProperties(userSession.userId(), limit);

    return ResponseEntity.ok(response);
  }
}
