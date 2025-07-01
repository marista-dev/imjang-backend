package com.imjang.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 관련 설정
 * JPA Auditing 기능을 활성화하여 엔티티의 생성/수정 시간을 자동으로 관리
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
  // JPA Auditing 활성화를 위한 설정 클래스
  // @CreatedDate, @LastModifiedDate 어노테이션이 동작하도록 함
}
