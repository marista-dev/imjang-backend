package com.imjang.domain.property.event;

import com.imjang.domain.property.location.service.LocationInfoService;
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

  private final LocationInfoService locationInfoService;

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

}
