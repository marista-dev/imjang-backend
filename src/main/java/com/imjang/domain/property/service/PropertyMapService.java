package com.imjang.domain.property.service;

import com.imjang.domain.property.dto.request.MapBoundsRequest;
import com.imjang.domain.property.dto.response.MapMarkersResponse;
import com.imjang.domain.property.dto.response.PropertyMarkerResponse;
import com.imjang.domain.property.dto.response.PropertySummaryCardResponse;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyMapService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final H3Util h3Util;

  /**
   * 지도 영역 내 매물 마커 조회
   */
  @Transactional(readOnly = true)
  public MapMarkersResponse getMapMarkers(MapBoundsRequest request, Long userId) {
    Set<String> h3Indices = h3Util.getH3IndicesForBounds(
            request.northEastLat(),
            request.northEastLng(),
            request.southWestLat(),
            request.southWestLng(),
            getH3Resolution(request.zoomLevel())
    );

    List<Property> properties = propertyRepository.findByUserIdAndH3IndexInAndDeletedAtIsNull(userId, h3Indices);

    List<PropertyMarkerResponse> markers = properties.stream()
            .map(PropertyMarkerResponse::from)
            .toList();

    return new MapMarkersResponse(markers);
  }

  /**
   * 매물 간략 정보 카드 조회
   */
  @Transactional(readOnly = true)
  public PropertySummaryCardResponse getPropertySummaryCard(Long propertyId, Long userId) {
    Property property = propertyRepository.findByIdAndDeletedAtIsNull(propertyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    if (!property.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    String thumbnailUrl = propertyImageRepository
            .findByPropertyIdAndDisplayOrder(propertyId, 0)
            .map(PropertyImage::getThumbnailUrl)
            .orElse(null);

    return new PropertySummaryCardResponse(
            property.getId(),
            property.getAddress(),
            property.getPriceType(),
            property.getDeposit(),
            property.getMonthlyRent(),
            property.getRating().doubleValue(),
            thumbnailUrl,
            property.getCreatedAt()
    );
  }

  /**
   * 줌 레벨에 따른 H3 해상도 결정
   */
  private int getH3Resolution(Integer zoomLevel) {
    if (zoomLevel >= 18) {
      return 11;  // 매우 상세
    } else if (zoomLevel >= 16) {
      return 10;
    } else if (zoomLevel >= 14) {
      return 9;   // 기본값
    } else {
      return 8;    // 광역
    }
  }
}
