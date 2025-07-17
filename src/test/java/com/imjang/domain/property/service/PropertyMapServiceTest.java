package com.imjang.domain.property.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.imjang.domain.auth.entity.User;
import com.imjang.domain.property.dto.request.MapBoundsRequest;
import com.imjang.domain.property.dto.response.MapMarkersResponse;
import com.imjang.domain.property.dto.response.PropertySummaryCardResponse;
import com.imjang.domain.property.entity.Property;
import com.imjang.domain.property.entity.PropertyImage;
import com.imjang.domain.property.entity.PropertyType;
import com.imjang.domain.property.location.util.H3Util;
import com.imjang.domain.property.repository.PropertyImageRepository;
import com.imjang.domain.property.repository.PropertyRepository;
import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyMapServiceTest {

  @InjectMocks
  private PropertyMapService propertyMapService;

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyImageRepository propertyImageRepository;

  @Mock
  private H3Util h3Util;

  @Test
  @DisplayName("지도 마커 조회 - 정상 케이스")
  void getMapMarkers_Success() {
    // Given
    MapBoundsRequest request = new MapBoundsRequest(
            37.5100, 127.0500, 37.4900, 127.0300, 15
    );
    Long userId = 1L;
    Set<String> h3Indices = Set.of("891f0d92b93ffff");

    Property property = mock(Property.class);
    when(property.getId()).thenReturn(1L);
    when(property.getLatitude()).thenReturn(37.5012);
    when(property.getLongitude()).thenReturn(127.0396);
    when(property.getRating()).thenReturn(4);

    List<Property> properties = List.of(property);

    given(h3Util.getH3IndicesForBounds(
            request.northEastLat(), request.northEastLng(),
            request.southWestLat(), request.southWestLng(), 9)
    ).willReturn(h3Indices);

    given(propertyRepository.findByUserIdAndH3IndexInAndDeletedAtIsNull(userId, h3Indices))
            .willReturn(properties);

    // When
    MapMarkersResponse response = propertyMapService.getMapMarkers(request, userId);

    // Then
    assertThat(response.markers()).hasSize(1);
    assertThat(response.markers().get(0).id()).isEqualTo(1L);
    assertThat(response.markers().get(0).latitude()).isEqualTo(37.5012);
    assertThat(response.markers().get(0).longitude()).isEqualTo(127.0396);

    verify(h3Util).getH3IndicesForBounds(
            request.northEastLat(), request.northEastLng(),
            request.southWestLat(), request.southWestLng(), 9);
    verify(propertyRepository).findByUserIdAndH3IndexInAndDeletedAtIsNull(userId, h3Indices);
  }

  @Test
  @DisplayName("지도 마커 조회 - 빈 결과")
  void getMapMarkers_EmptyResult() {
    // Given
    MapBoundsRequest request = new MapBoundsRequest(
            37.5100, 127.0500, 37.4900, 127.0300, 15
    );
    Long userId = 1L;
    Set<String> h3Indices = Set.of("891f0d92b93ffff");

    given(h3Util.getH3IndicesForBounds(
            request.northEastLat(), request.northEastLng(),
            request.southWestLat(), request.southWestLng(), 9))
            .willReturn(h3Indices);
    given(propertyRepository.findByUserIdAndH3IndexInAndDeletedAtIsNull(userId, h3Indices))
            .willReturn(Collections.emptyList());

    // When
    MapMarkersResponse response = propertyMapService.getMapMarkers(request, userId);

    // Then
    assertThat(response.markers()).isEmpty();
  }

  @Test
  @DisplayName("매물 간략 정보 조회")
  void getPropertySummaryCard_Success_WithThumbnail() {
    // Given
    Long propertyId = 1L;
    Long userId = 1L;

    User user = createTestUser(userId);
    Property property = createTestProperty(propertyId, user);
    PropertyImage propertyImage = createTestPropertyImage(propertyId);

    given(propertyRepository.findByIdAndDeletedAtIsNull(propertyId))
            .willReturn(Optional.of(property));
    given(propertyImageRepository.findByPropertyIdAndDisplayOrder(propertyId, 0))
            .willReturn(Optional.of(propertyImage));

    // When
    PropertySummaryCardResponse response = propertyMapService.getPropertySummaryCard(propertyId, userId);

    // Then
    assertThat(response.id()).isEqualTo(propertyId);
    assertThat(response.address()).isEqualTo("서초구 서초동 789-12");
    assertThat(response.priceType()).isEqualTo(PropertyType.JEONSE);
    assertThat(response.deposit()).isEqualTo(280000000L);
    assertThat(response.monthlyRent()).isEqualTo(0L);
    assertThat(response.rating()).isEqualTo(4.0);
    assertThat(response.thumbnailUrl()).isEqualTo("thumbnail.jpg");
    assertThat(response.visitedAt()).isNotNull();
  }

  @Test
  @DisplayName("매물 간략 정보 조회 - 정상 케이")
  void getPropertySummaryCard_Success_WithoutThumbnail() {
    // Given
    Long propertyId = 1L;
    Long userId = 1L;

    User user = createTestUser(userId);
    Property property = createTestProperty(propertyId, user);

    given(propertyRepository.findByIdAndDeletedAtIsNull(propertyId))
            .willReturn(Optional.of(property));
    given(propertyImageRepository.findByPropertyIdAndDisplayOrder(propertyId, 0))
            .willReturn(Optional.empty());

    // When
    PropertySummaryCardResponse response = propertyMapService.getPropertySummaryCard(propertyId, userId);

    // Then
    assertThat(response.thumbnailUrl()).isNull();
  }

  @Test
  @DisplayName("매물 간략 정보 조회 - 매물 없음 예외")
  void getPropertySummaryCard_PropertyNotFound() {
    // Given
    Long propertyId = 999L;
    Long userId = 1L;

    given(propertyRepository.findByIdAndDeletedAtIsNull(propertyId))
            .willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> propertyMapService.getPropertySummaryCard(propertyId, userId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROPERTY_NOT_FOUND);
  }

  @Test
  @DisplayName("매물 간략 정보 조회 - 권한 없음 예외")
  void getPropertySummaryCard_AccessDenied() {
    // Given
    Long propertyId = 1L;
    Long userId = 1L;
    Long otherUserId = 2L;

    User otherUser = createTestUser(otherUserId);
    Property property = createTestProperty(propertyId, otherUser);

    given(propertyRepository.findByIdAndDeletedAtIsNull(propertyId))
            .willReturn(Optional.of(property));

    // When & Then
    assertThatThrownBy(() -> propertyMapService.getPropertySummaryCard(propertyId, userId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
  }

  @Test
  @DisplayName("H3 해상도 결정 - 줌 레벨 18 이상")
  void getH3Resolution_ZoomLevel18OrHigher() throws Exception {
    // Given
    Integer zoomLevel = 18;

    // When
    int resolution = invokeGetH3Resolution(zoomLevel);

    // Then
    assertThat(resolution).isEqualTo(11);
  }

  @Test
  @DisplayName("H3 해상도 결정 - 줌 레벨 16-17")
  void getH3Resolution_ZoomLevel16To17() throws Exception {
    // Given
    Integer zoomLevel = 16;

    // When
    int resolution = invokeGetH3Resolution(zoomLevel);

    // Then
    assertThat(resolution).isEqualTo(10);
  }

  @Test
  @DisplayName("H3 해상도 결정 - 줌 레벨 14-15")
  void getH3Resolution_ZoomLevel14To15() throws Exception {
    // Given
    Integer zoomLevel = 14;

    // When
    int resolution = invokeGetH3Resolution(zoomLevel);

    // Then
    assertThat(resolution).isEqualTo(9);
  }

  @Test
  @DisplayName("H3 해상도 결정 - 줌 레벨 13 이하")
  void getH3Resolution_ZoomLevel13OrLower() throws Exception {
    // Given
    Integer zoomLevel = 13;

    // When
    int resolution = invokeGetH3Resolution(zoomLevel);

    // Then
    assertThat(resolution).isEqualTo(8);
  }

  @Test
  @DisplayName("H3 해상도 결정 - 경계값 테스트")
  void getH3Resolution_BoundaryValues() throws Exception {
    // Given & When & Then
    assertThat(invokeGetH3Resolution(18)).isEqualTo(11);
    assertThat(invokeGetH3Resolution(17)).isEqualTo(10);
    assertThat(invokeGetH3Resolution(16)).isEqualTo(10);
    assertThat(invokeGetH3Resolution(15)).isEqualTo(9);
    assertThat(invokeGetH3Resolution(14)).isEqualTo(9);
    assertThat(invokeGetH3Resolution(13)).isEqualTo(8);
    assertThat(invokeGetH3Resolution(1)).isEqualTo(8);
  }

  private User createTestUser(Long userId) {
    User user = mock(User.class, withSettings().lenient());
    lenient().when(user.getId()).thenReturn(userId);
    return user;
  }

  private Property createTestProperty(Long propertyId, User user) {
    Property property = mock(Property.class, withSettings().lenient());

    // Mock 설정
    lenient().when(property.getId()).thenReturn(propertyId);
    lenient().when(property.getUser()).thenReturn(user);
    lenient().when(property.getAddress()).thenReturn("서초구 서초동 789-12");
    lenient().when(property.getLatitude()).thenReturn(37.5012);
    lenient().when(property.getLongitude()).thenReturn(127.0396);
    lenient().when(property.getPriceType()).thenReturn(PropertyType.JEONSE);
    lenient().when(property.getDeposit()).thenReturn(280000000L);
    lenient().when(property.getMonthlyRent()).thenReturn(0L);
    lenient().when(property.getRating()).thenReturn(4);
    lenient().when(property.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());

    return property;
  }

  private PropertyImage createTestPropertyImage(Long propertyId) {
    Property mockProperty = mock(Property.class, withSettings().lenient());
    lenient().when(mockProperty.getId()).thenReturn(propertyId);

    PropertyImage propertyImage = mock(PropertyImage.class, withSettings().lenient());
    lenient().when(propertyImage.getThumbnailUrl()).thenReturn("thumbnail.jpg");
    lenient().when(propertyImage.getProperty()).thenReturn(mockProperty);

    return propertyImage;
  }

  private int invokeGetH3Resolution(Integer zoomLevel) throws Exception {
    Method method = PropertyMapService.class.getDeclaredMethod("getH3Resolution", Integer.class);
    method.setAccessible(true);
    return (int) method.invoke(propertyMapService, zoomLevel);
  }
}
