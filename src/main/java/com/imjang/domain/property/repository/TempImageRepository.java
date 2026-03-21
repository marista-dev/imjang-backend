package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.TempImage;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TempImageRepository extends JpaRepository<TempImage, Long> {

  List<TempImage> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

  void deleteByExpiresAtBefore(LocalDateTime dateTime);

  // 만료됐고 PropertyImage에 연결되지 않은 고아 TempImage 조회
  @Query("SELECT t FROM TempImage t WHERE t.expiresAt < :now AND NOT EXISTS (SELECT pi FROM PropertyImage pi WHERE pi.tempImageId = t.id)")
  List<TempImage> findExpiredAndUnlinked(@Param("now") LocalDateTime now, Pageable pageable);

}
