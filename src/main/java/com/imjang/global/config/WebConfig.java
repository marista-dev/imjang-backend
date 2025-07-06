package com.imjang.global.config;

import com.imjang.global.interceptor.AuthInterceptor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정 클래스
 * Spring MVC 설정을 위한 클래스
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;

  @Value("${app.upload.path}")
  private String uploadPath;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                    "/api/v1/auth/**",
                    "/api/health"
            );
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 임시 이미지 정적 리소스 핸들러
    registry.addResourceHandler("/temp-images/**")
            .addResourceLocations("file:" + uploadPath + "/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .resourceChain(true);
  }
}
