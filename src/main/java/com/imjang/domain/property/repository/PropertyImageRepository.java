package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.PropertyImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

  List<PropertyImage> findByPropertyIdOrderByDisplayOrder(Long propertyId);

  // 썸네일 이미지 조회 (displayOrder = 0)
  Optional<PropertyImage> findByPropertyIdAndDisplayOrder(Long propertyId, Integer displayOrder);

  // 여러 매물의 썸네일 이미지 한번에 조회
  List<PropertyImage> findByPropertyIdInAndDisplayOrder(List<Long> propertyIds, Integer displayOrder);
}
