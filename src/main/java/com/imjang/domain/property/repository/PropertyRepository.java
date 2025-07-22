package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.Property;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, Long> {

  @Query("SELECT p FROM Property p LEFT JOIN FETCH p.environments WHERE p.id = :id")
  Optional<Property> findByIdWithEnvironments(@Param("id") Long id);

  // 삭제되지 않은 매물만 조회
  Optional<Property> findByIdAndDeletedAtIsNull(Long id);

  // 최근 매물 조회 & 타임라인 매물 조회
  Page<Property> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

  // 전체 매물 개수 조회
  long countByUserIdAndDeletedAtIsNull(Long userId);

  // 이번 달 매물 개수 조회
  long countByUserIdAndDeletedAtIsNullAndCreatedAtBetween(Long userId,
                                                          LocalDateTime startOfMonth,
                                                          LocalDateTime endOfMonth);

  // H3 인덱스 기반 매물 조회 (삭제되지 않은 것만)
  List<Property> findByUserIdAndH3IndexInAndDeletedAtIsNull(Long userId, Set<String> h3Indices);
}
