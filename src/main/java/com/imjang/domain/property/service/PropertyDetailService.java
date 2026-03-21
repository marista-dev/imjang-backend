package com.imjang.domain.property.service;

import com.imjang.domain.property.dto.request.UpdatePropertyDetailRequest;
import com.imjang.domain.property.dto.response.EvaluationInfo;
import com.imjang.domain.property.dto.response.LocationDetailInfo;
import com.imjang.domain.property.dto.response.PropertyDetailResponse;
import com.imjang.domain.property.dto.response.PropertyImageDto;
import com.imjang.domain.property.dto.response.StationInfo;
import com.imjang.domain.property.dto.response.UpdatePropertyDetailResponse;
import com.imjang.domain.property.entity.ImageStatus;
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
    Property property = propertyRepository.findByIdWithEnvironments(propertyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    if (!property.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    if (property.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.PROPERTY_NOT_FOUND);
    }

    List<PropertyImageDto> images = getPropertyImages(propertyId);

    // 평가 정보 생성
    EvaluationInfo evaluation = new EvaluationInfo(
            property.isMoveInAvailable(),
            property.isRevisitIntention(),
            property.getPriceEvaluation()
    );

    LocationDetailInfo locationInfo = getLocationDetailInfo(property);

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
            images,
            evaluation,
            property.getParkingType(),
            property.getEnvironments(),
            property.getMemo(),
            locationInfo
    );
  }

  /**
   * 매물 이미지 목록 조회.
   * COMPLETED 이미지 우선 반환, 없으면 PENDING fallback (업로드 전 신규 매물 대응).
   */
  private List<PropertyImageDto> getPropertyImages(Long propertyId) {
    List<PropertyImage> all = propertyImageRepository.findByPropertyIdOrderByDisplayOrder(propertyId);

    List<PropertyImage> completed = all.stream()
            .filter(img -> img.getStatus() == ImageStatus.COMPLETED)
            .toList();

    List<PropertyImage> display = completed.isEmpty()
            ? all.stream().filter(img -> img.getStatus() == ImageStatus.PENDING).toList()
            : completed;

    return display.stream()
            .map(img -> new PropertyImageDto(img.getId(), img.getThumbnailUrl(), img.getImageUrl()))
            .toList();
  }

  /**
   * 위치 상세 정보 조회
   */
  private LocationDetailInfo getLocationDetailInfo(Property property) {
    if (property.getH3Index() == null) {
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

              //TODO 버스 정보 (현재는 null - 추후 구현)
              StationInfo bus = null;

              return new LocationDetailInfo(
                      subway,
                      bus,
                      locationInfo.amenityInfos()
              );
            })
            .orElse(null);
  }

  /**
   * 매물 상세 정보 수정
   */
  @Transactional
  public UpdatePropertyDetailResponse updatePropertyDetail(Long propertyId,
                                                           UpdatePropertyDetailRequest request,
                                                           Long userId) {
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    if (!property.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    if (property.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.PROPERTY_NOT_FOUND);
    }

    property.updateDetails(
            request.moveInAvailable(),
            request.revisitIntention(),
            request.priceEvaluation(),
            request.parkingType(),
            request.maintenanceFee(),
            request.environments(),
            request.memo()
    );

    return new UpdatePropertyDetailResponse(
            property.getId(),
            property.isMoveInAvailable(),
            property.isRevisitIntention(),
            property.getPriceEvaluation(),
            property.getParkingType(),
            property.getMaintenanceFee(),
            property.getEnvironments(),
            property.getMemo(),
            property.getUpdatedAt()
    );
  }
}
