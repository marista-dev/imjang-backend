package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.property.dto.request.AddPropertyImagesRequest;
import com.imjang.domain.property.dto.response.AddPropertyImagesResponse;
import com.imjang.domain.property.service.PropertyService;
import com.imjang.global.annotation.LoginRequired;
import com.imjang.global.common.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PropertyImage", description = "매물 이미지 관리 API")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyImageController {

  private final PropertyService propertyService;

  @Operation(summary = "매물 이미지 추가",
          description = "기존 매물에 이미지를 추가. 임시 이미지 ID 목록을 전달받아 매물 이미지로 변환.")
  @PostMapping("/{propertyId}/images")
  @LoginRequired
  public ResponseEntity<AddPropertyImagesResponse> addImagesToProperty(
          @Parameter(description = "매물 ID", required = true, example = "1")
          @PathVariable Long propertyId,
          @Valid @RequestBody AddPropertyImagesRequest request,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

    AddPropertyImagesResponse response = propertyService.addImagesToProperty(
            propertyId,
            request.tempImageIds(),
            userSession.userId()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "매물 이미지 삭제",
          description = "매물에 등록된 특정 이미지를 삭제. Soft Delete 방식으로 처리, 실제 파일은 스케줄러에 의해 비동기로 삭제")
  @DeleteMapping("/{propertyId}/images/{imageId}")
  @LoginRequired
  public ResponseEntity<MessageResponse> deletePropertyImage(
          @Parameter(description = "매물 ID", required = true, example = "1")
          @PathVariable Long propertyId,
          @Parameter(description = "삭제할 이미지 ID", required = true, example = "10")
          @PathVariable Long imageId,
          HttpServletRequest servletRequest) {
    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");

    propertyService.deletePropertyImage(propertyId, imageId, userSession.userId());

    return ResponseEntity.ok(MessageResponse.of("이미지가 성공적으로 삭제되었습니다."));
  }
}
