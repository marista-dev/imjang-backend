package com.imjang.domain.property.location.repository;

import com.imjang.domain.property.location.entity.LocationCache;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationCacheRepository extends JpaRepository<LocationCache, Long> {

  Optional<LocationCache> findByH3Index(String h3Index);

  @Query("SELECT lc "
          + "FROM LocationCache lc "
          + "WHERE lc.h3Index = :h3Index "
          + "AND lc.lastFetchedAt > :validDate")
  Optional<LocationCache> findValidCacheByH3Index(
          @Param("h3Index") String h3Index,
          @Param("validDate") LocalDateTime validDate
  );
}
