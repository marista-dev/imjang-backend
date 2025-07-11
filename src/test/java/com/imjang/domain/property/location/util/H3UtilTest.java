package com.imjang.domain.property.location.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class H3UtilTest {

  private H3Util h3Util;

  @BeforeEach
  void setUp() throws Exception {
    h3Util = new H3Util();
  }

  @Test
  @DisplayName("같은 지역의 좌표는 같은 H3 인덱스를 반환한다")
  void shouldReturnSameH3IndexForNearbyCoordinates() {
    // Given - 강남역 주변 좌표들
    Double lat1 = 37.4979;
    Double lng1 = 127.0276;

    Double lat2 = 37.4980;  // 약 11m 차이
    Double lng2 = 127.0277;

    // When
    String h3Index1 = h3Util.getH3Index(lat1, lng1);
    String h3Index2 = h3Util.getH3Index(lat2, lng2);

    // Then
    assertThat(h3Index1).isEqualTo(h3Index2);
  }

  @Test
  @DisplayName("다른 지역의 좌표는 다른 H3 인덱스를 반환한다")
  void shouldReturnDifferentH3IndexForDistantCoordinates() {
    // Given
    Double gangnamLat = 37.4979;
    Double gangnamLng = 127.0276;

    Double sinchonLat = 37.5597;  // 신촌역
    Double sinchonLng = 126.9424;

    // When
    String gangnamH3 = h3Util.getH3Index(gangnamLat, gangnamLng);
    String sinchonH3 = h3Util.getH3Index(sinchonLat, sinchonLng);

    // Then
    assertThat(gangnamH3).isNotEqualTo(sinchonH3);
  }
}
