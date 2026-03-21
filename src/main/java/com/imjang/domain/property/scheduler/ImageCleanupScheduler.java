package com.imjang.domain.property.scheduler;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.event.PropertyCreatedEvent;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.common.event.DomainEventPublisher;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이미지 정리 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupScheduler {

  private final TempImageRepository tempImageRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final DomainEventPublisher domainEventPublisher;

  /**
   * 매시간 실행: S3 업로드 완료된 로컬 임시 파일 삭제
   */
  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void deleteCompletedLocalFiles() {
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    List<PropertyImage> completedImages = propertyImageRepository.findByStatusAndUpdatedAtBefore(
            ImageStatus.COMPLETED, oneHourAgo, PageRequest.of(0, 100));

    List<Long> tempImageIds = completedImages.stream()
            .map(PropertyImage::getTempImageId)
            .filter(id -> id != null)
            .toList();

    if (!tempImageIds.isEmpty()) {
      tempImageRepository.findAllById(tempImageIds).forEach(tempImage -> {
        try {
          deleteLocalFile(tempImage.getOriginalUrl());
          deleteLocalFile(tempImage.getThumbnailUrl());
        } catch (Exception e) {
          log.error("로컬 파일 삭제 실패: tempImageId={}", tempImage.getId(), e);
        }
      });
    }
    log.info("S3 업로드 완료된 로컬 파일 {} 개 처리 완료", completedImages.size());
  }

  /**
   * 매시간 실행: 만료된 미연결 TempImage 삭제
   */
  @Scheduled(cron = "0 30 * * * *")
  @Transactional
  public void deleteExpiredTempImages() {
    log.info("만료된 임시 이미지 삭제 시작");

    List<TempImage> expiredImages = tempImageRepository.findExpiredAndUnlinked(
            LocalDateTime.now(), PageRequest.of(0, 100));

    for (TempImage tempImage : expiredImages) {
      try {
        deleteLocalFile(tempImage.getOriginalUrl());
        deleteLocalFile(tempImage.getThumbnailUrl());
        tempImageRepository.delete(tempImage);
      } catch (Exception e) {
        log.error("만료된 임시 이미지 삭제 실패: tempImageId={}", tempImage.getId(), e);
      }
    }

    log.info("만료된 임시 이미지 {} 개 처리 완료", expiredImages.size());
  }

  /**
   * 10분마다 실행: 생성 후 3시간 이내 FAILED 이미지 재시도
   */
  @Scheduled(cron = "0 */10 * * * *")
  @Transactional
  public void retryFailedUploads() {
    log.info("실패한 이미지 업로드 재시도 시작");

    LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

    List<PropertyImage> failedImages = propertyImageRepository.findByStatusAndCreatedAtAfter(
            ImageStatus.FAILED, threeHoursAgo, PageRequest.of(0, 20));

    for (PropertyImage image : failedImages) {
      try {
        domainEventPublisher.publishAfterCommit(new PropertyCreatedEvent(
                image.getProperty().getId(),
                List.of(image.getId())
        ));
        log.info("이미지 업로드 재시도: imageId={}", image.getId());
      } catch (Exception e) {
        log.error("재시도 실패: imageId={}", image.getId(), e);
      }
    }

    log.info("실패한 이미지 {} 개 재시도 완료", failedImages.size());
  }

  /**
   * 로컬 파일 삭제
   */
  private void deleteLocalFile(String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      return;
    }

    try {
      File file = new File(filePath);
      if (file.exists() && file.delete()) {
        log.debug("파일 삭제 완료: {}", filePath);
      }
    } catch (Exception e) {
      log.warn("파일 삭제 실패: {}", filePath, e);
    }
  }
}