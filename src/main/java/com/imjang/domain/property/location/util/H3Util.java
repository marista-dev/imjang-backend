package com.imjang.domain.property.location.util;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class H3Util {

  private static final int H3_RESOLUTION = 9;
  private final H3Core h3Core;

  public H3Util() throws IOException {
    this.h3Core = H3Core.newInstance();
  }

  /**
   * 위도, 경도를 H3 인덱스로 변환
   */
  public String getH3Index(Double latitude, Double longitude) {
    return h3Core.latLngToCellAddress(latitude, longitude, H3_RESOLUTION);
  }

  /**
   * H3 인덱스의 중심점 좌표 조회
   */
  public LatLng getH3Center(String h3Index) {
    return h3Core.cellToLatLng(h3Index);
  }

  /**
   * 지도 영역(viewport)에 포함되는 H3 인덱스 집합 반환
   */
  public Set<String> getH3IndicesForBounds(Double northEastLat, Double northEastLng,
                                           Double southWestLat, Double southWestLng,
                                           int resolution) {
    try {
      // viewport를 polygon으로 변환 (시계방향)
      List<LatLng> boundary = List.of(
              new LatLng(southWestLat, southWestLng),
              new LatLng(northEastLat, southWestLng),
              new LatLng(northEastLat, northEastLng),
              new LatLng(southWestLat, northEastLng),
              new LatLng(southWestLat, southWestLng)
      );

      List<Long> h3IndicesLong = h3Core.polygonToCells(boundary, null, resolution);

      Set<String> h3Indices = h3IndicesLong.stream()
              .map(h3Core::h3ToString)
              .collect(java.util.stream.Collectors.toSet());

      log.debug("Viewport H3 변환: resolution={}, count={}", resolution, h3Indices.size());

      return h3Indices;

    } catch (Exception e) {
      log.error("H3 인덱스 집합 생성 실패: bounds=({},{}) to ({},{})",
              southWestLat, southWestLng, northEastLat, northEastLng, e);
      throw new RuntimeException("지도 영역 처리 중 오류가 발생했습니다.", e);
    }
  }
}
