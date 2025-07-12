package com.imjang.domain.property.repository;

import com.imjang.domain.property.entity.PropertyImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

  List<PropertyImage> findByPropertyIdOrderByDisplayOrder(Long propertyId);
}
