package com.imjang.domain.property.location.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imjang.domain.property.location.dto.AmenityInfo;
import com.imjang.domain.property.location.dto.LocationInfo;
import com.imjang.domain.property.location.dto.TransitInfo;
import com.imjang.domain.property.location.entity.LocationCache;
import com.imjang.domain.property.location.repository.LocationCacheRepository;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.infrastructure.kakao.KaKaoApiClient;
import com.imjang.infrastructure.kakao.dto.KakaoCategoryCode;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchRequest;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationInfoService {

  private final LocationCacheRepository locationCacheRepository;
  private final KaKaoApiClient kakaoApiClient;
  private final H3Util h3Util;
  private final ObjectMapper objectMapper;

  private static final int CACHE_VALID_DAYS = 30;
  private static final int TRANSIT_SEARCH_RADIUS = 1000;
  private static final int AMENITY_SEARCH_RADIUS = 500;

  /**
   * 위치 정보를 비동기로 수집, 캐시 저장
   * 매물 생성 시 호출됨
   */
  @Async("locationTaskExecutor")
  public void fetchAndCacheLocationInfo(Double latitude, Double longitude) {
    try {
      String h3Index = h3Util.getH3Index(latitude, longitude);

      LocalDateTime validDate = LocalDateTime.now().minusDays(CACHE_VALID_DAYS);
      Optional<LocationCache> existingCache = locationCacheRepository.findValidCacheByH3Index(h3Index, validDate);

      if (existingCache.isPresent()) {
        log.info("위치 정보가 이미 캐시에 존재: {}", h3Index);
        return;
      }
      log.info("새로운 위치 정보 조회 시작 - h3Index: {}", h3Index);
      fetchAndSaveLocationInfo(h3Index, latitude, longitude);
    } catch (Exception e) {
      log.error("위치 정보 수집 실패 - 좌표: ({}, {})", latitude, longitude, e);
    }
  }

  /**
   * 위치 정보를 조회하고 캐시에 저장
   */
  private void fetchAndSaveLocationInfo(String h3Index, Double latitude, Double longitude) {
    // 병렬로 API 호출
    Mono<TransitInfo> transitInfoMono = fetchTransitInfo(latitude, longitude);
    Mono<List<AmenityInfo>> amenityInfoMono = fetchAmenityInfo(latitude, longitude);

    // 결과 조합 및 저장
    Mono.zip(transitInfoMono, amenityInfoMono)
            .subscribe(tuple -> {
              TransitInfo transitInfo = tuple.getT1();
              List<AmenityInfo> amenityInfos = tuple.getT2();

              // 캐시에 저장
              saveToCache(h3Index, latitude, longitude, transitInfo, amenityInfos);
            }, error -> {
              log.error("위치 정보 조회 실패 - h3Index: {}", h3Index, error);
            });
  }

  /**
   * H3 인덱스로 캐시된 위치 정보 조회
   * 매물 상세 조회 시 사용
   */
  @Transactional(readOnly = true)
  public Optional<LocationInfo> getLocationInfoByH3Index(String h3Index) {
    if (h3Index == null) {
      return Optional.empty();
    }

    return locationCacheRepository.findByH3Index(h3Index)
            .map(this::convertCacheToLocationInfo);
  }

  /**
   * 대중교통 정보 조회
   */
  private Mono<TransitInfo> fetchTransitInfo(Double lat, Double lng) {
    // 지하철역 검색
    Mono<KakaoCategorySearchResponse> subwayMono = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(
                    KakaoCategoryCode.SUBWAY.getCode(),
                    lng,
                    lat,
                    TRANSIT_SEARCH_RADIUS
            )
    );

    return subwayMono.map(response -> {
      if (!response.documents().isEmpty()) {
        KakaoCategorySearchResponse.Document nearest = response.documents().get(0);
        int distance = Integer.parseInt(nearest.distance());
        return new TransitInfo(
                nearest.placeName(),
                distance,
                calculateWalkTime(distance),
                0  // 버스 정보는 추후 구현
        );
      }

      return TransitInfo.empty();
    });
  }

  /**
   * 편의시설 정보 조회
   */
  private Mono<List<AmenityInfo>> fetchAmenityInfo(Double lat, Double lng) {
    // 조회할 카테고리 목록
    List<KakaoCategoryCode> amenityCategories = Arrays.asList(
            KakaoCategoryCode.CONVENIENCE_STORE,
            KakaoCategoryCode.MART,
            KakaoCategoryCode.BANK,
            KakaoCategoryCode.HOSPITAL,
            KakaoCategoryCode.PHARMACY
    );

    // 병렬로 모든 카테고리 검색
    List<Mono<KakaoCategorySearchResponse>> searchMonos = amenityCategories.stream()
            .map(category -> kakaoApiClient.searchByCategory(
                    KakaoCategorySearchRequest.of(
                            category.getCode(),
                            lng,
                            lat,
                            AMENITY_SEARCH_RADIUS
                    )
            ))
            .collect(Collectors.toList());

    return Mono.zip(searchMonos, responses -> {
      List<AmenityInfo> amenityInfos = new ArrayList<>();

      for (int i = 0; i < responses.length; i++) {
        KakaoCategorySearchResponse response = (KakaoCategorySearchResponse) responses[i];
        KakaoCategoryCode category = amenityCategories.get(i);

        String nearestName = null;
        Integer nearestDistance = null;

        if (!response.documents().isEmpty()) {
          KakaoCategorySearchResponse.Document nearest = response.documents().get(0);
          nearestName = nearest.placeName();
          nearestDistance = Integer.parseInt(nearest.distance());
        }

        amenityInfos.add(new AmenityInfo(
                category.getDescription(),
                category.getCode(),
                response.documents().size(),
                nearestName,
                nearestDistance
        ));
      }

      return amenityInfos;
    });
  }

  /**
   * 캐시에 데이터 저장
   */
  private void saveToCache(String h3Index, Double lat, Double lng,
                           TransitInfo transitInfo, List<AmenityInfo> amenityInfos) {
    try {
      LocationCache cache = locationCacheRepository.findByH3Index(h3Index)
              .orElse(LocationCache.builder()
                      .h3Index(h3Index)
                      .centerLat(lat)
                      .centerLng(lng)
                      .build());

      String transitData = objectMapper.writeValueAsString(transitInfo);
      String amenitiesData = objectMapper.writeValueAsString(amenityInfos);

      cache.updateData(transitData, amenitiesData);
      locationCacheRepository.save(cache);

      log.info("위치 정보 캐시 저장 완료 - h3Index: {}", h3Index);

    } catch (Exception e) {
      log.error("캐시 저장 실패 - h3Index: {}", h3Index, e);
    }
  }

  /**
   * 캐시 데이터를 LocationInfo로 변환
   */
  private LocationInfo convertCacheToLocationInfo(LocationCache cache) {
    try {
      TransitInfo transitInfo = objectMapper.readValue(
              cache.getTransitData(), TransitInfo.class);
      List<AmenityInfo> amenityInfos = objectMapper.readValue(
              cache.getAmenitiesData(),
              objectMapper.getTypeFactory().constructCollectionType(List.class, AmenityInfo.class)
      );

      return LocationInfo.of(cache.getH3Index(), transitInfo, amenityInfos);

    } catch (Exception e) {
      log.error("캐시 데이터 변환 실패", e);
      throw new RuntimeException("캐시 데이터 변환 실패", e);
    }
  }

  /**
   * 거리를 도보 시간 변환 (분속 80m 기준)
   */
  private int calculateWalkTime(int distanceInMeters) {
    return (int) Math.ceil(distanceInMeters / 80.0);
  }

}
