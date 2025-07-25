package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.PropertyImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

  List<PropertyImage> findByPropertyIdOrderByDisplayOrder(Long propertyId);

  // 썸네일 이미지 조회 (displayOrder = 0)
  Optional<PropertyImage> findByPropertyIdAndDisplayOrder(Long propertyId, Integer displayOrder);

  // 여러 매물의 썸네일 이미지 한번에 조회
  List<PropertyImage> findByPropertyIdInAndDisplayOrder(List<Long> propertyIds, Integer displayOrder);

  @Query("update PropertyImage p set p.status = :status where p.property.id = :propertyId")
  @Modifying
  int updateStatusByPropertyId(@Param("propertyId") Long propertyId, @Param("status") ImageStatus status);

  // 삭제된 이미지 정리용 쿼리 (DB 레벨 필터링)
  @Query("SELECT pi FROM PropertyImage pi WHERE pi.status = :status AND pi.updatedAt < :cutoffDate")
  Page<PropertyImage> findByStatusAndUpdatedAtBefore(@Param("status") ImageStatus status,
                                                     @Param("cutoffDate") LocalDateTime cutoffDate,
                                                     Pageable pageable);

  Optional<PropertyImage> findByIdAndPropertyIdAndStatusNot(Long id, Long propertyId, ImageStatus status);
}
