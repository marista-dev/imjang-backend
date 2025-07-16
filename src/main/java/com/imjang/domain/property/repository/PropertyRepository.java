package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.Property;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, Long> {

  @Query("SELECT p FROM Property p LEFT JOIN FETCH p.environments WHERE p.id = :id")
  Optional<Property> findByIdWithEnvironments(@Param("id") Long id);

  // 최근 매물 조회
  Page<Property> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

  // 전체 매물 개수 조회
  long countByUserIdAndDeletedAtIsNull(Long userId);

  // 이번 달 매물 개수 조회
  long countByUserIdAndDeletedAtIsNullAndCreatedAtBetween(Long userId,
                                                          LocalDateTime startOfMonth,
                                                          LocalDateTime endOfMonth);
}
