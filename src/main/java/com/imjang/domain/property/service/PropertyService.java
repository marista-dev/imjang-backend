package com.imjang.domain.property.service;

import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final TempImageRepository tempImageRepository;
  private final UserRepository userRepository;

  /**
   * 매물 빠른 기록 생성
   */
  @Transactional
  public void createProperty(CreatePropertyRequest request, Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 이미지 검증
    List<TempImage> tempImages = validateAndGetTempImages(request.imageIds(), userId);

    // 매물 엔티티 생성
    Property property = Property.builder()
            .user(user)
            .address(request.address())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .priceType(request.priceType())
            .deposit(request.deposit())
            .monthlyRent(request.monthlyRent())
            .price(request.price())
            .area(request.area())
            .currentFloor(request.currentFloor())
            .totalFloor(request.totalFloors())
            .rating(request.rating())
            .priceEvaluation(request.priceEvaluation())
            .moveInAvailable(request.moveInAvailable())
            .revisitIntention(request.revisitIntention())
            .maintenanceFee(request.maintenanceFee())
            .memo(request.memo())
            .build();

    property = propertyRepository.save(property);
    linkImagesToProperty(property, tempImages);
  }

  /**
   * 임시 이미지 검증 및 조회
   */
  private List<TempImage> validateAndGetTempImages(List<Long> imageIds, Long userId) {
    if (imageIds == null || imageIds.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 사용자의 임시 이미지만 조회
    List<TempImage> tempImages = tempImageRepository.findByUserIdAndIdIn(userId, imageIds);

    if (tempImages.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }

    return tempImages;
  }

  /**
   * 임시 이미지를 매물 이미지로 연결
   */
  private void linkImagesToProperty(Property property, List<TempImage> tempImages) {
    List<PropertyImage> propertyImages = IntStream.range(0, tempImages.size())
            .mapToObj(i -> PropertyImage.builder()
                    .property(property)
                    .tempImageId(tempImages.get(i).getId())
                    .imageUrl(tempImages.get(i).getOriginalUrl())
                    .thumbnailUrl(tempImages.get(i).getThumbnailUrl())
                    .displayOrder(i)
                    .build())
            .toList();
    propertyImageRepository.saveAll(propertyImages);
  }


}
