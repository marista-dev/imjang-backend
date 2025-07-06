package com.imjang.domain.property.event;

import java.util.List;

public record PropertyCreatedEvent(
        Long propertyId,
        List<Long> propertyImageIds
) {

}
