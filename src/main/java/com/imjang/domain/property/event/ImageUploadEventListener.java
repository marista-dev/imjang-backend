package com.imjang.domain.property.event;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
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
   * 매물 생성 시 이미지 S3 업로드.
   * 트랜잭션 커밋 후 실행되어 PropertyImage가 DB에 확실히 존재함을 보장.
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

    propertyImageRepository.saveAll(images);
  }

  /**
   * 개별 이미지 S3 업로드. 실패 시 FAILED 상태로 마킹하고 다음 이미지 처리를 계속함.
   */
  private void uploadImageToS3(PropertyImage image) {
    try {
      image.updateStatus(ImageStatus.UPLOADING);

      TempImage tempImage = tempImageRepository.findById(image.getTempImageId())
              .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

      File originalFile = new File(tempImage.getOriginalUrl());
      File thumbnailFile = new File(tempImage.getThumbnailUrl());

      if (!originalFile.exists() || !thumbnailFile.exists()) {
        throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
      }

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

      image.updateUrls(s3ImageUrl, s3ThumbnailUrl);

      log.info("✅이미지 S3 업로드 완료: imageId={}, propertyId={}",
              image.getId(), image.getProperty().getId());

    } catch (Exception e) {
      log.error("❌이미지 S3 업로드 실패: imageId={}", image.getId(), e);
      image.updateStatus(ImageStatus.FAILED);
    }
  }
}