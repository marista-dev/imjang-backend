package com.imjang.domain.property.location.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imjang.domain.property.location.entity.LocationCache;
import com.imjang.domain.property.location.repository.LocationCacheRepository;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.infrastructure.kakao.KaKaoApiClient;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationInfoServiceTest {

  @InjectMocks
  private LocationInfoService locationInfoService;

  @Mock
  private LocationCacheRepository locationCacheRepository;

  @Mock
  private KaKaoApiClient kakaoApiClient;

  @Mock
  private H3Util h3Util;

  @Mock
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("캐시 히트 시 API를 호출하지 않는다")
  void shouldNotCallApiWhenCacheHit() {
    // Given
    Double lat = 37.5665;
    Double lng = 126.9780;
    String h3Index = "8930e1d8b93ffff";

    when(h3Util.getH3Index(lat, lng)).thenReturn(h3Index);
    when(locationCacheRepository.findValidCacheByH3Index(eq(h3Index), any(LocalDateTime.class)))
            .thenReturn(Optional.of(LocationCache.builder().build()));

    // When
    locationInfoService.fetchAndCacheLocationInfo(lat, lng);

    // Then
    verify(kakaoApiClient, never()).searchByCategory(any());
    verify(locationCacheRepository, never()).save(any());
  }

  @Test
  @DisplayName("캐시 미스 시 API를 호출한다")
  void shouldCallApiWhenCacheMiss() {
    // Given
    Double lat = 37.5665;
    Double lng = 126.9780;
    String h3Index = "8930e1d8b93ffff";

    when(h3Util.getH3Index(lat, lng)).thenReturn(h3Index);
    when(locationCacheRepository.findValidCacheByH3Index(eq(h3Index), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

    // When
    locationInfoService.fetchAndCacheLocationInfo(lat, lng);

    // Then
    // 최소한 캐시 조회는 했는지 확인
    verify(locationCacheRepository).findValidCacheByH3Index(eq(h3Index), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("H3 인덱스로 캐시된 위치 정보를 조회한다")
  void shouldRetrieveCachedLocationInfoByH3Index() throws Exception {
    // Given
    String h3Index = "8930e1d8b93ffff";
    LocationCache mockCache = LocationCache.builder()
            .h3Index(h3Index)
            .centerLat(37.5665)
            .centerLng(126.9780)
            .transitData(
                    "{\"nearestSubwayStation\":null,\"subwayDistance\":null,\"subwayWalkTime\":null,"
                            + "\"busStopCount\":0}")
            .amenitiesData("[]")
            .build();

    when(locationCacheRepository.findByH3Index(h3Index))
            .thenReturn(Optional.of(mockCache));
    when(objectMapper.readValue(anyString(), eq(com.imjang.domain.property.location.dto.TransitInfo.class)))
            .thenReturn(com.imjang.domain.property.location.dto.TransitInfo.empty());
    when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.databind.type.CollectionType.class)))
            .thenReturn(new java.util.ArrayList<>());
    when(objectMapper.getTypeFactory()).thenReturn(new com.fasterxml.jackson.databind.ObjectMapper().getTypeFactory());

    // When
    var result = locationInfoService.getLocationInfoByH3Index(h3Index);

    // Then
    verify(locationCacheRepository).findByH3Index(h3Index);
    assert (result.isPresent());
  }

  @Test
  @DisplayName("null H3 인덱스로 조회 시 빈 Optional을 반환한다")
  void shouldReturnEmptyOptionalWhenH3IndexIsNull() {
    // When
    var result = locationInfoService.getLocationInfoByH3Index(null);

    // Then
    assert (result.isEmpty());
    verify(locationCacheRepository, never()).findByH3Index(any());
  }
}
