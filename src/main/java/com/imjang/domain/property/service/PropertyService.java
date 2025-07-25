package com.imjang.domain.property.service;

import com.imjang.domain.auth.entity.User;
import com.imjang.domain.auth.repository.UserRepository;
import com.imjang.domain.property.dto.request.CreatePropertyRequest;
import com.imjang.domain.property.dto.response.AddPropertyImagesResponse;
import com.imjang.domain.property.dto.response.PriceInfoResponse;
import com.imjang.domain.property.dto.response.PropertySummaryResponse;
import com.imjang.domain.property.dto.response.PropertyTimelineResponse;
import com.imjang.domain.property.dto.response.RecentPropertyResponse;
import com.imjang.domain.property.dto.response.TimelineGroupResponse;
import com.imjang.domain.property.dto.response.TimelinePropertyResponse;
import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.TempImage;
import com.imjang.domain.property.event.PropertyCreatedEvent;
import com.imjang.domain.property.location.entity.LocationCache;
import com.imjang.domain.property.location.repository.LocationCacheRepository;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.domain.property.repository.TempImageRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private final LocationCacheRepository locationCacheRepository;
  private final H3Util h3Util;

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

    // H3 인덱스 계산 및 위치 정보 조회
    String h3Index = null;
    LocationCache locationCache = null;

    try {
      h3Index = h3Util.getH3Index(request.latitude(), request.longitude());
      log.debug("H3 인덱스 계산 완료: lat={}, lng={}, h3Index={}",
              request.latitude(), request.longitude(), h3Index);
    } catch (IllegalArgumentException | ArithmeticException e) {
      // 좌표값 오류, H3 계산 오류
      log.warn("H3 인덱스 계산 실패 (잘못된 좌표값): lat={}, lng={}, error={}",
              request.latitude(), request.longitude(), e.getMessage());
    } catch (Exception e) {
      // 예상치 못한 시스템 오류 - 에러 로그 남기고 재시도 가능하도록 예외 전파
      log.error("H3 인덱스 계산 중 시스템 오류 발생: lat={}, lng={}",
              request.latitude(), request.longitude(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // location_cache 조회 (H3 인덱스가 있는 경우에만)
    if (h3Index != null) {
      try {
        locationCache = locationCacheRepository.findByH3Index(h3Index).orElse(null);
        log.info("매물 생성: h3Index={}, locationCached={}", h3Index, locationCache != null);
      } catch (Exception e) {
        // DB 조회 실패는 시스템 오류이므로 예외 전파
        log.error("위치 캐시 조회 실패: h3Index={}", h3Index, e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      log.info("매물 생성: h3Index=null (H3 계산 실패로 인한 기본값), locationCached=false");
    }

    // 매물 엔티티 생성
    Property property = Property.builder()
            .user(user)
            .address(request.address())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .h3Index(h3Index)
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
            .parkingType(request.parkingType())
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
   * 매물에 이미지 추가
   */
  @Transactional
  public AddPropertyImagesResponse addImagesToProperty(Long propertyId, List<Long> tempImageIds, Long userId) {

    // 1. Property 소유권 검증
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    if (!property.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 2. TempImage 유효성 검증
    List<TempImage> tempImages = validateAndGetTempImages(tempImageIds, userId);

    // 3. 현재 최대 displayOrder 조회
    List<PropertyImage> existingImages = propertyImageRepository.findByPropertyIdOrderByDisplayOrder(propertyId);
    int nextDisplayOrder = existingImages.isEmpty() ? 0 :
            existingImages.getLast().getDisplayOrder() + 1;

    // 4. PropertyImage 생성 및 저장
    List<PropertyImage> propertyImages = createPropertyImages(property, tempImages, nextDisplayOrder);

    List<PropertyImage> savedImages = propertyImageRepository.saveAll(propertyImages);

    // 5. PropertyImageAddedEvent 발행
    List<Long> imageIds = savedImages.stream()
            .map(PropertyImage::getId)
            .toList();

    eventPublisher.publishEvent(new PropertyCreatedEvent(propertyId, imageIds));

    log.info("매물 이미지 추가 완료: propertyId={}, addedCount={}", propertyId, imageIds.size());

    return AddPropertyImagesResponse.of(imageIds);
  }

  /**
   * 최근 매물 목록 조회
   */
  @Transactional(readOnly = true)
  public RecentPropertyResponse getRecentProperties(Long userId, int limit) {
    PageRequest pageRequest = PageRequest.of(0, limit);
    Page<Property> propertyPage = propertyRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            userId, pageRequest);
    List<Property> properties = propertyPage.getContent();

    List<Long> propertyIds = properties.stream()
            .map(Property::getId)
            .toList();

    List<PropertyImage> thumbnails;
    if (propertyIds.isEmpty()) {
      thumbnails = List.of();
    } else {
      thumbnails = propertyImageRepository.findByPropertyIdInAndDisplayOrder(propertyIds, 0);
    }

    Map<Long, String> thumbnailMap = thumbnails.stream()
            .collect(Collectors.toMap(
                    img -> img.getProperty().getId(),
                    PropertyImage::getThumbnailUrl
            ));

    List<PropertySummaryResponse> propertySummaries = properties.stream()
            .map(property -> new PropertySummaryResponse(
                    property.getId(),
                    property.getAddress(),
                    property.getCreatedAt(),
                    property.getPriceType(),
                    new PriceInfoResponse(
                            property.getDeposit(),
                            property.getMonthlyRent(),
                            property.getPrice()
                    ),
                    property.getRating(),
                    thumbnailMap.get(property.getId())
            ))
            .toList();

    long totalCount = propertyRepository.countByUserIdAndDeletedAtIsNull(userId);

    LocalDateTime startOfMonth = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

    long monthlyCount = propertyRepository.countByUserIdAndDeletedAtIsNullAndCreatedAtBetween(
            userId, startOfMonth, endOfMonth);

    return new RecentPropertyResponse(propertySummaries, totalCount, monthlyCount);
  }

  /**
   * 타임라인 조회
   */
  @Transactional(readOnly = true)
  public PropertyTimelineResponse getPropertyTimeline(Long userId, Pageable pageable) {
    Page<Property> propertyPage = propertyRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            userId, pageable);
    List<Property> properties = propertyPage.getContent();

    if (properties.isEmpty()) {
      return new PropertyTimelineResponse(List.of(), false);
    }

    List<Long> propertyIds = properties.stream()
            .map(Property::getId)
            .toList();

    List<PropertyImage> thumbnails = propertyImageRepository.findByPropertyIdInAndDisplayOrder(
            propertyIds, 0);
    Map<Long, String> thumbnailMap = thumbnails.stream()
            .collect(Collectors.toMap(
                    img -> img.getProperty().getId(),
                    PropertyImage::getImageUrl
            ));
    Map<LocalDate, List<TimelinePropertyResponse>> groupedByDate = new LinkedHashMap<>();
    for (Property property : properties) {
      LocalDate date = property.getCreatedAt().toLocalDate();
      TimelinePropertyResponse timelineProperty = new TimelinePropertyResponse(
              property.getId(),
              property.getCreatedAt(),
              property.getAddress(),
              property.getRating(),
              property.getPriceType(),
              property.getDeposit(),
              property.getMonthlyRent(),
              property.getArea(),
              property.getCurrentFloor(),
              property.getTotalFloor(),
              thumbnailMap.get(property.getId())
      );

      groupedByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(timelineProperty);
    }
    List<TimelineGroupResponse> timelineGroups = groupedByDate.entrySet().stream()
            .map(entry -> new TimelineGroupResponse(entry.getKey(), entry.getValue()))
            .toList();
    boolean hasNext = propertyPage.hasNext();
    return new PropertyTimelineResponse(timelineGroups, hasNext);
  }

  /**
   * 매물 삭제
   */
  @Transactional
  public void deleteProperty(Long propertyId, Long userId) {
    Property property = propertyRepository.findByIdAndUserIdAndDeletedAtIsNull(propertyId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    property.softDelete();

    int updatedImageCount = propertyImageRepository.updateStatusByPropertyId(propertyId, ImageStatus.DELETED);

    log.info("매물 삭제 완료: propertyId={}, userId={}, deletedImageCount={}",
            propertyId, userId, updatedImageCount);
  }

  /**
   * 매물 이미지 단일 삭제
   */
  @Transactional
  public void deletePropertyImage(Long propertyId, Long imageId, Long userId) {
    Property property = propertyRepository.findByIdAndUserIdAndDeletedAtIsNull(propertyId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

    PropertyImage propertyImage = propertyImageRepository
            .findByIdAndPropertyIdAndStatusNot(imageId, propertyId, ImageStatus.DELETED)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

    propertyImage.updateStatus(ImageStatus.DELETED);
  }

  /**
   * 임시 이미지를 매물 이미지로 연결
   */
  private List<PropertyImage> linkImagesToProperty(Property property, List<TempImage> tempImages) {
    List<PropertyImage> propertyImages = createPropertyImages(property, tempImages, 0);
    return propertyImageRepository.saveAll(propertyImages);
  }

  /**
   * PropertyImage 엔티티 생성
   */
  private List<PropertyImage> createPropertyImages(Property property, List<TempImage> tempImages,
                                                   int startDisplayOrder) {
    return IntStream.range(0, tempImages.size())
            .mapToObj(i -> {
              TempImage tempImage = tempImages.get(i);

              String tempImageUrl = convertToWebPath(tempImage.getOriginalUrl());
              String tempThumbnailUrl = convertToWebPath(tempImage.getThumbnailUrl());

              return PropertyImage.builder()
                      .property(property)
                      .tempImageId(tempImage.getId())
                      .imageUrl(tempImageUrl)
                      .thumbnailUrl(tempThumbnailUrl)
                      .displayOrder(startDisplayOrder + i)
                      .build();
            })
            .toList();
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
    return 0L;
  }
}
