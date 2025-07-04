package com.imjang.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스 접근 설정
 * 업로드된 이미지 파일에 대한 URL 매핑
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  @Value("${app.upload.path:uploads}")
  private String uploadPath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // /images/** URL을 실제 업로드 디렉토리와 매핑
    registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + uploadPath + "/")
            .setCachePeriod(3600); // 1시간 캐시
  }
}
