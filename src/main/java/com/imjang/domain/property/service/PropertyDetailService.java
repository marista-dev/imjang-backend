package com.imjang.domain.property.service;

import com.imjang.domain.property.dto.response.EvaluationInfo;
import com.imjang.domain.property.dto.response.LocationDetailInfo;
import com.imjang.domain.property.dto.response.PropertyDetailResponse;
import com.imjang.domain.property.dto.response.StationInfo;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.location.dto.TransitInfo;
import com.imjang.domain.property.location.service.LocationInfoService;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyDetailService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final LocationInfoService locationInfoService;

  /**
   * 매물 상세 정보 조회
   */
  @Transactional(readOnly = true)
  public PropertyDetailResponse getPropertyDetail(Long propertyId, Long userId) {
    // 1. Property 조회
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    // 2. 권한 검증 (자신의 매물만 조회 가능)
    if (!property.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 3. Soft delete check
    if (property.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.PROPERTY_NOT_FOUND);
    }

    // 4. 이미지 조회
    List<String> imageUrls = getPropertyImageUrls(propertyId);

    // 5. 평가 정보 생성
    EvaluationInfo evaluation = new EvaluationInfo(
            property.isMoveInAvailable(),
            property.isRevisitIntention(),
            property.getPriceEvaluation()
    );

    // 6. 위치 상세 정보 조회
    LocationDetailInfo locationInfo = getLocationDetailInfo(property);

    // 7. PropertyDetailResponse 생성
    return new PropertyDetailResponse(
            property.getId(),
            property.getAddress(),
            property.getCreatedAt(),
            property.getPriceType(),
            property.getDeposit(),
            property.getMonthlyRent(),
            property.getPrice(),
            property.getMaintenanceFee(),
            property.getArea(),
            property.getCurrentFloor(),
            property.getTotalFloor(),
            property.getRating(),
            imageUrls,
            evaluation,
            property.getMemo(),
            locationInfo
    );
  }

  /**
   * 매물 이미지 URL 목록 조회
   */
  private List<String> getPropertyImageUrls(Long propertyId) {
    List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByDisplayOrder(
            propertyId);

    return images.stream()
            .map(PropertyImage::getThumbnailUrl)
            .toList();
  }

  /**
   * 위치 상세 정보 조회
   */
  private LocationDetailInfo getLocationDetailInfo(Property property) {
    // H3 인덱스가 없거나 위치 정보 조회가 완료되지 않은 경우 null 반환
    if (property.getH3Index() == null
            || property.getLocationFetchStatus() != com.imjang.domain.property.entity.LocationFetchStatus.COMPLETED) {
      return null;
    }

    // 캐시된 위치 정보 조회
    return locationInfoService.getLocationInfoByH3Index(property.getH3Index())
            .map(locationInfo -> {
              TransitInfo transitInfo = locationInfo.transitInfo();

              // 지하철 정보
              StationInfo subway = null;
              if (transitInfo != null && transitInfo.nearestSubwayStation() != null) {
                subway = new StationInfo(
                        transitInfo.nearestSubwayStation(),
                        transitInfo.subwayDistance(),
                        transitInfo.subwayWalkTime()
                );
              }

              // 버스 정보 (현재는 null - 추후 구현)
              StationInfo bus = null;

              return new LocationDetailInfo(
                      subway,
                      bus,
                      locationInfo.amenityInfos(),
                      property.getLocationFetchStatus(),
                      property.getLocationFetchedAt()
              );
            })
            .orElse(null);
  }
}
