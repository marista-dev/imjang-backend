package com.imjang.domain.property.event;

import com.imjang.domain.property.location.service.LocationInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class PropertyEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private LocationInfoService locationInfoService;

    @Test
    @DisplayName("LocationPrefetchEvent 발행 시 비동기로 처리된다")
    void shouldProcessLocationPrefetchEventAsynchronously() {
        // Given
        LocationPrefetchEvent event = new LocationPrefetchEvent(
                37.5665,
                126.9780,
                "서울시 중구",
                1L
        );

        // When
        eventPublisher.publishEvent(event);

        // Then - 3초 내에 비동기로 호출되었는지 확인
        verify(locationInfoService, timeout(3000))
                .fetchAndCacheLocationInfo(anyDouble(), anyDouble());
    }
}