package com.imjang.domain.property.event;

import com.imjang.domain.property.entity.ImageUploadStatus;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.infrastructure.s3.S3Service;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이미지 업로드 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadEventListener {

  private final PropertyImageRepository propertyImageRepository;
  private final TempImageRepository tempImageRepository;
  private final S3Service s3Service;

  /**
   * 매물 생성 시 이미지 S3 업로드
   */
  @Async("imageUploadExecutor")
  @EventListener
  @Transactional
  public void handlePropertyCreated(PropertyCreatedEvent event) {
    log.info("⬆이미지 S3 업로드 시작: propertyId={}", event.propertyId());

    List<PropertyImage> images = propertyImageRepository.findAllById(event.propertyImageIds());

    images.sort((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()));

    for (PropertyImage image : images) {
      uploadImageToS3(image);
    }
  }

  /**
   * 개별 이미지 S3 업로드
   */
  private void uploadImageToS3(PropertyImage image) {
    try {
      image.updateStatus(ImageUploadStatus.UPLOADING);
      propertyImageRepository.save(image);

      // TempImage에서 실제 파일 경로 가져오기
      TempImage tempImage = tempImageRepository.findById(image.getTempImageId())
              .orElseThrow(() -> new RuntimeException("TempImage not found: " + image.getTempImageId()));

      // 로컬 파일 찾기
      File originalFile = new File(tempImage.getOriginalUrl());
      File thumbnailFile = new File(tempImage.getThumbnailUrl());

      if (!originalFile.exists() || !thumbnailFile.exists()) {
        throw new RuntimeException("이미지 파일이 존재하지 않습니다");
      }

      // S3 업로드
      String s3ImageUrl = s3Service.uploadImage(
              originalFile,
              image.getProperty().getId(),
              originalFile.getName()
      );

      String s3ThumbnailUrl = s3Service.uploadThumbnail(
              thumbnailFile,
              image.getProperty().getId(),
              thumbnailFile.getName()
      );

      // URL 업데이트 및 상태 변경
      image.updateUrls(s3ImageUrl, s3ThumbnailUrl);
      propertyImageRepository.save(image);

      log.info("✅이미지 S3 업로드 완료: imageId={}, propertyId={}",
              image.getId(), image.getProperty().getId());

    } catch (Exception e) {
      log.error("❌이미지 S3 업로드 실패: imageId={}", image.getId(), e);
      image.updateStatus(ImageUploadStatus.FAILED);
      propertyImageRepository.save(image);
    }
  }
}
