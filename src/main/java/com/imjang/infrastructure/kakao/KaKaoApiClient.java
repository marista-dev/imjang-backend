package com.imjang.infrastructure.kakao;

import com.imjang.global.exception.CustomException;
import com.imjang.global.exception.ErrorCode;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchRequest;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class KaKaoApiClient {

  private final WebClient.Builder webClientBuilder;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  @Value("${kakao.api.baseurl}")
  private String kakaoBaseUrl;

  private WebClient webClient;
  private RateLimiter rateLimiter;

  @PostConstruct
  public void init() {
    this.webClient = webClientBuilder
            .baseUrl(kakaoBaseUrl)
            .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
            .build();

    RateLimiterConfig config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(10)
            .timeoutDuration(Duration.ofSeconds(5))
            .build();

    this.rateLimiter = RateLimiter.of("kakao-api", config);
  }

  /**
   * 카테고리로 장소 검색
   */
  public Mono<KakaoCategorySearchResponse> searchByCategory(KakaoCategorySearchRequest request) {
    return executeWithRateLimit(() ->
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", request.categoryGroupCode())
                            .queryParam("x", request.x())
                            .queryParam("y", request.y())
                            .queryParam("radius", request.radius())
                            .queryParam("page", request.page())
                            .queryParam("size", request.size())
                            .queryParam("sort", request.sort())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new CustomException(ErrorCode.EXTERNAL_API_BAD_REQUEST)))
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR)))
                    .bodyToMono(KakaoCategorySearchResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof CustomException &&
                                    ((CustomException) throwable).getErrorCode() == ErrorCode.EXTERNAL_API_ERROR))
                    .doOnError(error -> log.error("카테고리 검색 실패: {}", request, error))
    );
  }

  /**
   * Rate Limiting 적용 API 호출
   */
  private <T> Mono<T> executeWithRateLimit(java.util.function.Supplier<Mono<T>> supplier) {
    return Mono.fromSupplier(() -> {
              boolean permission = rateLimiter.acquirePermission();
              if (!permission) {
                log.warn("Rate limit exceeded for Kakao API");
              }
              return permission;
            })
            .flatMap(hasPermission -> {
              if (!hasPermission) {
                return Mono.error(new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED));
              }
              return supplier.get();
            });
  }
}
