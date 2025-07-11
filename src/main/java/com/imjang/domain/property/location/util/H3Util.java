package com.imjang.domain.property.location.util;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.io.IOException;
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
}
