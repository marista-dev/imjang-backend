package com.imjang.domain.property.event;

import com.imjang.domain.property.entity.LocationFetchStatus;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.location.service.LocationInfoService;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매물 주변시설 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyEventListener {

  private final PropertyRepository propertyRepository;
  private final LocationInfoService locationInfoService;
  private final H3Util h3Util;

  /**
   * 위치 정보 사전 수집
   */
  @Async("locationTaskExecutor")
  @EventListener
  @Transactional
  public void handleLocationPrefetch(LocationPrefetchEvent event) {
    log.info("📍 위치 정보 사전 수집 시작: 좌표=({}, {})", event.latitude(), event.longitude());

    try {
      // 위치 정보 수집
      locationInfoService.fetchAndCacheLocationInfo(
              event.latitude(),
              event.longitude()
      );
      log.info("✅ 위치 정보 사전 수집 완료: 좌표=({}, {})", event.latitude(), event.longitude());
    } catch (Exception e) {
      log.error("❌ 위치 정보 사전 수집 실패: 좌표=({}, {})", event.latitude(), event.longitude(), e);
    }
  }

  /**
   * 매물 위치 정보 수집
   */
  @Async("locationTaskExecutor")
  @EventListener
  @Transactional
  public void handlePropertyCreatedForLocation(PropertyCreatedEvent event) {
    log.info("📍 위치 정보 수집 시작: propertyId={}", event.propertyId());

    try {
      Property property = propertyRepository.findById(event.propertyId())
              .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

      // H3 인덱스 계산 및 저장
      String h3Index = h3Util.getH3Index(
              property.getLatitude(),
              property.getLongitude()
      );
      property.updateH3Index(h3Index);

      // 상태를 PROCESSING으로 변경
      property.updateLocationFetchStatus(LocationFetchStatus.PROCESSING);
      propertyRepository.save(property);

      // 위치 정보 수집
      locationInfoService.fetchAndCacheLocationInfo(
              property.getLatitude(),
              property.getLongitude()
      );
      log.info("✅ 위치 정보 수집 완료: propertyId={}, h3Index={}",
              event.propertyId(), h3Index);
    } catch (Exception e) {
      log.error("❌ 위치 정보 수집 실패: propertyId={}", event.propertyId(), e);

      // 실패 시 상태 업데이트
      propertyRepository.findById(event.propertyId())
              .ifPresent(property -> {
                property.updateLocationFetchStatus(LocationFetchStatus.FAILED);
                propertyRepository.save(property);
              });
    }
  }
}
