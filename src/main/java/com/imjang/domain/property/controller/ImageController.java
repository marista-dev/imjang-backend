package com.imjang.domain.property.controller;

import com.imjang.domain.auth.dto.UserSession;
import com.imjang.domain.property.dto.response.ImageUploadResponse;
import com.imjang.domain.property.service.ImageService;
import com.imjang.global.annotation.LoginRequired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image", description = "이미지 관련 API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @Operation(summary = "이미지 업로드", description = "이미지 업로드 후 썸네일 생성")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LoginRequired
  public ResponseEntity<ImageUploadResponse> uploadImage(
          @RequestPart("image") MultipartFile image,
          HttpServletRequest servletRequest) {

    UserSession userSession = (UserSession) servletRequest.getAttribute("USER_SESSION");
    ImageUploadResponse response = imageService.uploadImage(image, userSession.userId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
