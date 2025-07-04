package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.TempImage;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempImageRepository extends JpaRepository<TempImage, Long> {

  List<TempImage> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

  void deleteByExpiresAtBefore(LocalDateTime dateTime);

}
