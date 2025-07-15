package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.Property;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, Long> {

  @Query("SELECT p FROM Property p LEFT JOIN FETCH p.environments WHERE p.id = :id")
  Optional<Property> findByIdWithEnvironments(@Param("id") Long id);

}
