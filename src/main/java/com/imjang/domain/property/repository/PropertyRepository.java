package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

}
