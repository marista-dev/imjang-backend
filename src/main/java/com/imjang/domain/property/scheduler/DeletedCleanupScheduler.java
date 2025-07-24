package com.imjang.domain.property.scheduler;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.infrastructure.s3.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeletedCleanupScheduler {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final S3Service s3Service;

  @Value("${app.cleanup.retention-days:30}")
  private int retentionDays;

  @Value("${app.cleanup.batch-size:100}")
  private int batchSize;

  @Scheduled(cron = "0 0 3 * * SUN")
  @Transactional
  public void cleanupDeletedProperties() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
    PageRequest pageRequest = PageRequest.of(0, batchSize);

    Page<Property> deletedPropertyPage = propertyRepository.findDeletedPropertiesBeforeDate(
            cutoffDate, pageRequest);
    List<Property> deletedProperties = deletedPropertyPage.getContent();

    if (deletedProperties.isEmpty()) {
      log.info("삭제할 매물이 없습니다.");
      return;
    }

    log.info("삭제 대상 매물 수: {}", deletedProperties.size());

    int successCount = 0;
    int failCount = 0;

    for (Property property : deletedProperties) {
      try {
        cleanupPropertyImages(property.getId());

        propertyRepository.delete(property);

        successCount++;
        log.info("매물 삭제 완료: propertyId={}", property.getId());
      } catch (Exception e) {
        failCount++;
        log.error("매물 삭제 실패: propertyId={}, error={}",
                property.getId(), e.getMessage(), e);
      }
    }
  }

  /**
   * 매물과 관련된 이미지들을 S3에서 삭제, DB 제거
   */
  private void cleanupPropertyImages(Long propertyId) {
    List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByDisplayOrder(propertyId);

    for (PropertyImage image : images) {
      try {
        // S3에서 이미지 삭제 (COMPLETED 상태인 것만)
        if (image.getStatus() == ImageStatus.COMPLETED) {
          deleteFromS3(image);
        }

        // DB에서 이미지 레코드 삭제
        propertyImageRepository.delete(image);

      } catch (Exception e) {
        log.error("이미지 삭제 실패: imageId={}, error={}",
                image.getId(), e.getMessage());
      }
    }

    log.debug("매물 이미지 삭제 완료: propertyId={}, imageCount={}",
            propertyId, images.size());
  }

  /**
   * S3에서 이미지 파일 삭제
   */
  private void deleteFromS3(PropertyImage image) {
    try {
      // URL에서 S3 키 추출
      String imageKey = extractS3Key(image.getImageUrl());
      if (imageKey != null) {
        s3Service.deleteFile(imageKey);
      }

      // 썸네일 삭제
      String thumbnailKey = extractS3Key(image.getThumbnailUrl());
      if (thumbnailKey != null) {
        s3Service.deleteFile(thumbnailKey);
      }

    } catch (Exception e) {
      log.warn("S3 이미지 삭제 실패 (무시하고 계속): imageId={}", image.getId());
    }
  }

  /**
   * S3 URL에서 키 추출
   * 예: https://bucket.s3.region.amazonaws.com/images/123/filename.jpg -> images/123/filename.jpg
   */
  private String extractS3Key(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    try {
      // S3 URL 패턴에서 키 추출
      int startIndex = url.indexOf(".com/");
      if (startIndex != -1) {
        return url.substring(startIndex + 5);
      }

      // CloudFront URL 등 다른 패턴 처리
      String[] parts = url.split("/", 4);
      if (parts.length >= 4) {
        return parts[3];
      }

    } catch (Exception e) {
      log.warn("S3 키 추출 실패: {}", url);
    }

    return null;
  }

  /**
   * 삭제된 이미지 정리 (DELETED 상태인 이미지들)
   * 매일 새벽 3시 30분에 실행
   */
  @Scheduled(cron = "0 30 3 * * *")
  @Transactional
  public void cleanupDeletedImages() {
    log.info("삭제된 이미지 정리 작업 시작");

    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
    PageRequest pageRequest = PageRequest.of(0, batchSize);

    Page<PropertyImage> deletedImagePage = propertyImageRepository.findByStatusAndUpdatedAtBefore(
            ImageStatus.DELETED, cutoffDate, pageRequest);
    List<PropertyImage> deletedImages = deletedImagePage.getContent();

    int deletedCount = 0;

    for (PropertyImage image : deletedImages) {
      try {
        // S3에서 삭제
        if (image.getStatus() == ImageStatus.COMPLETED) {
          deleteFromS3(image);
        }

        // DB에서 삭제
        propertyImageRepository.delete(image);
        deletedCount++;

      } catch (Exception e) {
        log.error("이미지 삭제 실패: imageId={}", image.getId(), e);
      }
    }

    log.info("삭제된 이미지 정리 완료: {}개", deletedCount);
  }

  /**
   * 스케줄러 상태 확인 (매일 오전 9시)
   * 운영팀에서 모니터링할 수 있도록 로그 출력
   */
  @Scheduled(cron = "0 0 9 * * *")
  public void reportCleanupStatus() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

    // 삭제 대기 중인 매물 수
    long pendingProperties = propertyRepository.findAll().stream()
            .filter(p -> p.getDeletedAt() != null)
            .filter(p -> p.getDeletedAt().isBefore(cutoffDate))
            .count();

    // 삭제 대기 중인 이미지 수
    long pendingImages = propertyImageRepository.findAll().stream()
            .filter(img -> img.getStatus() == ImageStatus.DELETED)
            .filter(img -> img.getUpdatedAt().isBefore(cutoffDate))
            .count();

    log.info("===== 삭제 데이터 정리 현황 =====");
    log.info("보관 기간: {}일", retentionDays);
    log.info("삭제 대기 매물: {}개", pendingProperties);
    log.info("삭제 대기 이미지: {}개", pendingImages);
    log.info("다음 정리 일정: 일요일 새벽 3시");
    log.info("================================");
  }
}
