package com.imjang.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정
 */
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            .info(apiInfo())
            .servers(getServers())
            .components(new Components()
                    .addSecuritySchemes("bearerAuth", bearerAuthSecurityScheme()))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

  private Info apiInfo() {
    return new Info()
            .title("임장기록장 API")
            .description("부동산 매물 방문 기록 관리 서비스 API 문서")
            .version("v1.0.0")
            .contact(new Contact()
                    .name("임장기록장 팀")
                    .email("support@imjang.com"))
            .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0"));
  }

  private List<Server> getServers() {
    return List.of(
            new Server()
                    .url("http://localhost:8080")
                    .description("Local server"),
            new Server()
                    .url("https://api.imjang.com")
                    .description("Production server")
    );
  }

  private SecurityScheme bearerAuthSecurityScheme() {
    return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)");
  }
}
