package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.ImageStatus;
import com.imjang.domain.property.entity.PropertyImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

  // 매물 상세용: 특정 상태 이미지만 조회
  List<PropertyImage> findByPropertyIdAndStatusOrderByDisplayOrder(Long propertyId, ImageStatus status);

  @Query("update PropertyImage p set p.status = :status where p.property.id = :propertyId")
  @Modifying
  int updateStatusByPropertyId(@Param("propertyId") Long propertyId, @Param("status") ImageStatus status);

  // 스케줄러용: 상태별 + 업데이트 시간 기준
  @Query("SELECT pi FROM PropertyImage pi WHERE pi.status = :status AND pi.updatedAt < :before")
  List<PropertyImage> findByStatusAndUpdatedAtBefore(@Param("status") ImageStatus status,
                                                     @Param("before") LocalDateTime before,
                                                     Pageable pageable);

  // 재시도용: FAILED 상태 + 생성 시간 기준
  @Query("SELECT pi FROM PropertyImage pi WHERE pi.status = :status AND pi.createdAt > :after")
  List<PropertyImage> findByStatusAndCreatedAtAfter(@Param("status") ImageStatus status,
                                                    @Param("after") LocalDateTime after,
                                                    Pageable pageable);

  Optional<PropertyImage> findByIdAndPropertyIdAndStatusNot(Long id, Long propertyId, ImageStatus status);
}
