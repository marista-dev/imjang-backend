package com.imjang.domain.property.scheduler;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.event.PropertyCreatedEvent;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 매시간 실행: S3 업로드 완료된 로컬 파일 삭제
   */
  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void deleteCompletedLocalFiles() {
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    // COMPLETED 상태이고 1시간 이상 지난 이미지 조회
    List<PropertyImage> completedImages = propertyImageRepository.findAll().stream()
            .filter(img -> img.getStatus() == ImageStatus.COMPLETED)
            .filter(img -> img.getUpdatedAt().isBefore(oneHourAgo))
            .limit(100)
            .toList();
    for (PropertyImage image : completedImages) {
      try {
        // TempImage 조회하여 로컬 파일 경로 확인
        if (image.getTempImageId() != null) {
          tempImageRepository.findById(image.getTempImageId())
                  .ifPresent(tempImage -> {
                    deleteLocalFile(tempImage.getOriginalUrl());
                    deleteLocalFile(tempImage.getThumbnailUrl());
                  });
        }
      } catch (Exception e) {
        log.error("로컬 파일 삭제 실패: imageId={}", image.getId(), e);
      }
    }
    log.info("S3 업로드 완료된 로컬 파일 {} 개 처리 완료", completedImages.size());
  }

  /**
   * 매시간 실행: 만료된 temp_images 삭제
   */
  @Scheduled(cron = "0 30 * * * *")
  @Transactional
  public void deleteExpiredTempImages() {
    log.info("만료된 임시 이미지 삭제 시작");

    LocalDateTime now = LocalDateTime.now();

    // 만료된 TempImage 조회 (연결된 PropertyImage가 없는 것만)
    List<TempImage> expiredImages = tempImageRepository.findAll().stream()
            .filter(img -> img.getExpiresAt().isBefore(now))
            .limit(100)
            .toList();

    for (TempImage tempImage : expiredImages) {
      try {
        // PropertyImage와 연결되지 않은 것만 삭제
        boolean isLinked = propertyImageRepository.findAll().stream()
                .anyMatch(pi -> tempImage.getId().equals(pi.getTempImageId()));

        if (!isLinked) {
          // 로컬 파일 삭제
          deleteLocalFile(tempImage.getOriginalUrl());
          deleteLocalFile(tempImage.getThumbnailUrl());

          // DB 레코드 삭제
          tempImageRepository.delete(tempImage);
        }
      } catch (Exception e) {
        log.error("만료된 임시 이미지 삭제 실패: tempImageId={}", tempImage.getId(), e);
      }
    }

    log.info("만료된 임시 이미지 {} 개 처리 완료", expiredImages.size());
  }

  /**
   * 10분마다 실행: 실패한 업로드 재시도
   */
  @Scheduled(cron = "0 */10 * * * *")
  @Transactional
  public void retryFailedUploads() {
    log.info("실패한 이미지 업로드 재시도 시작");

    LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

    // FAILED 상태이고 10분 이상 지난 이미지 조회
    List<PropertyImage> failedImages = propertyImageRepository.findAll().stream()
            .filter(img -> img.getStatus() == ImageStatus.FAILED)
            .filter(img -> img.getUpdatedAt().isBefore(tenMinutesAgo))
            .limit(20)
            .toList();

    for (PropertyImage image : failedImages) {
      try {
        // 최대 3회까지만 재시도 (createdAt과 updatedAt 차이로 대략 계산)
        long hoursElapsed = java.time.Duration.between(
                image.getCreatedAt(),
                LocalDateTime.now()
        ).toHours();

        if (hoursElapsed < 3) {
          // 재시도를 위해 이벤트 발행
          eventPublisher.publishEvent(new PropertyCreatedEvent(
                  image.getProperty().getId(),
                  List.of(image.getId())
          ));
          log.info("이미지 업로드 재시도: imageId={}", image.getId());
        } else {
          log.warn("최대 재시도 횟수 초과: imageId={}", image.getId());
        }
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
