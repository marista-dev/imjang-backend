package com.imjang.domain.property.service;

import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.event.PropertyCreatedEvent;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  @Value("${app.upload.path}")
  private String uploadPath;

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
    List<PropertyImage> savedImages = linkImagesToProperty(property, tempImages);
    List<Long> imageIds = savedImages.stream()
            .map(PropertyImage::getId)
            .toList();
    eventPublisher.publishEvent(new PropertyCreatedEvent(property.getId(), imageIds));
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
  private List<PropertyImage> linkImagesToProperty(Property property, List<TempImage> tempImages) {
    List<PropertyImage> propertyImages = IntStream.range(0, tempImages.size())
            .mapToObj(i -> {
              TempImage tempImage = tempImages.get(i);

              // 파일 시스템 경로를 웹 경로로 변환
              String tempImageUrl = convertToWebPath(tempImage.getOriginalUrl());
              String tempThumbnailUrl = convertToWebPath(tempImage.getThumbnailUrl());

              return PropertyImage.builder()
                      .property(property)
                      .tempImageId(tempImage.getId())
                      .imageUrl(tempImageUrl)
                      .thumbnailUrl(tempThumbnailUrl)
                      .displayOrder(i)
                      .build();
            })
            .toList();

    return propertyImageRepository.saveAll(propertyImages);
  }

  /**
   * 파일 시스템 경로를 웹 접근 가능한 경로로 변환
   */
  private String convertToWebPath(String filePath) {
    try {
      Path absolutePath = Paths.get(filePath).toAbsolutePath();
      Path basePath = Paths.get(uploadPath).toAbsolutePath();
      Path relativePath = basePath.relativize(absolutePath);

      String webPath = relativePath.toString().replace(File.separator, "/");
      return "/temp-images/" + webPath;

    } catch (Exception e) {
      log.error("경로 변환 실패: {}", filePath, e);
      return "/api/v1/images/temp/" + extractTempImageId(filePath) + "/thumbnail";
    }
  }

  /**
   * 파일 경로에서 tempImageId 추출 (fallback용)
   */
  private Long extractTempImageId(String filePath) {
    // 이 로직은 실제로는 사용되지 않을 예정
    // convertToWebPath가 실패할 경우를 대비한 안전장치
    return 0L;
  }
}
