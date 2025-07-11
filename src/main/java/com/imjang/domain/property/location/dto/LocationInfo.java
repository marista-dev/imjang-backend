package com.imjang.domain.property.location.dto;

import java.util.List;

public record LocationInfo(
        String h3Index,
        TransitInfo transitInfo,
        List<AmenityInfo> amenityInfos
) {

  public static LocationInfo of(String h3Index, TransitInfo transitInfo, List<AmenityInfo> amenityInfos) {
    return new LocationInfo(h3Index, transitInfo, amenityInfos);
  }
}
