package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.property.dto.request.MapBoundsRequest;
import com.imjang.domain.property.dto.response.MapMarkersResponse;
import com.imjang.domain.property.dto.response.PropertySummaryCardResponse;
import com.imjang.domain.property.service.PropertyMapService;
import com.imjang.global.annotation.LoginRequired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Property Map", description = "매물 지도 관련 API")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyMapController {

  private final PropertyMapService propertyMapService;

  @Operation(summary = "지도 범위 내 마커 매물 조회",
          description = "현재 보이는 지도 영역(viewport) 내의 매물 위치 정보를 조회")
  @GetMapping("/map/markers")
  @LoginRequired
  public ResponseEntity<MapMarkersResponse> getMapMarkers(
          @Valid @ModelAttribute MapBoundsRequest request,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");
    MapMarkersResponse response = propertyMapService.getMapMarkers(request, userSession.userId());

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "매물 간략 정보 조회",
          description = "지도에서 마커 선택 시 표시할 매물의 간략 정보를 조회")
  @GetMapping("/{propertyId}/summary")
  @LoginRequired
  public ResponseEntity<PropertySummaryCardResponse> getPropertySummaryCard(
          @Parameter(description = "매물 ID", required = true, example = "1")
          @PathVariable Long propertyId,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");
    PropertySummaryCardResponse response = propertyMapService.getPropertySummaryCard(
            propertyId,
            userSession.userId()
    );

    return ResponseEntity.ok(response);
  }
}
