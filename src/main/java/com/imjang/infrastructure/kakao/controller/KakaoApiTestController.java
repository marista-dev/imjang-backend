package com.imjang.infrastructure.kakao.controller;

import com.imjang.infrastructure.kakao.KaKaoApiClient;
import com.imjang.infrastructure.kakao.dto.KakaoCategoryCode;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchRequest;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Kakao API Test", description = "카카오 API 테스트용 엔드포인트 (개발 환경 전용)")
@RestController
@RequestMapping("/api/v1/test/kakao")
@RequiredArgsConstructor
@Profile({"local", "dev"})  // 개발 환경에서만 활성화
@Slf4j
public class KakaoApiTestController {

  private final KaKaoApiClient kakaoApiClient;

  @Operation(summary = "카테고리로 장소 검색", description = "지정된 좌표 주변의 특정 카테고리 장소를 검색합니다.")
  @GetMapping("/search/category")
  public Mono<Map<String, Object>> searchByCategory(
          @Parameter(description = "경도", example = "127.02479803562213") @RequestParam Double lng,
          @Parameter(description = "위도", example = "37.504585233865086") @RequestParam Double lat,
          @Parameter(description = "카테고리 코드", example = "CS2") @RequestParam String categoryCode,
          @Parameter(description = "검색 반경(미터)", example = "500") @RequestParam(defaultValue = "500") Integer radius
  ) {
    log.info("카테고리 검색 요청 - 좌표: ({}, {}), 카테고리: {}, 반경: {}m", lng, lat, categoryCode, radius);

    KakaoCategorySearchRequest request = KakaoCategorySearchRequest.of(categoryCode, lng, lat, radius);

    return kakaoApiClient.searchByCategory(request)
            .map(response -> {
              Map<String, Object> result = new HashMap<>();
              result.put("총 검색 결과", response.meta().totalCount());
              result.put("검색된 장소 수", response.documents().size());
              result.put("장소 목록", response.documents().stream()
                      .map(doc -> Map.of(
                              "이름", doc.placeName(),
                              "주소", doc.addressName(),
                              "거리", doc.distance() + "m",
                              "전화번호", doc.phone() != null ? doc.phone() : "없음"
                      ))
                      .collect(Collectors.toList()));
              return result;
            });
  }

  @Operation(summary = "주변 편의시설 통합 검색", description = "지정된 좌표 주변의 편의점, 지하철역, 은행, 병원을 한번에 검색합니다.")
  @GetMapping("/search/nearby")
  public Mono<Map<String, Object>> searchNearbyFacilities(
          @Parameter(description = "경도", example = "127.02479803562213") @RequestParam Double lng,
          @Parameter(description = "위도", example = "37.504585233865086") @RequestParam Double lat
  ) {
    log.info("주변 편의시설 통합 검색 - 좌표: ({}, {})", lng, lat);

    // 편의점 검색 (500m)
    Mono<KakaoCategorySearchResponse> convenienceStore = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.CONVENIENCE_STORE.getCode(), lng, lat, 500)
    );

    // 지하철역 검색 (1000m)
    Mono<KakaoCategorySearchResponse> subway = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.SUBWAY.getCode(), lng, lat, 1000)
    );

    // 은행 검색 (500m)
    Mono<KakaoCategorySearchResponse> bank = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.BANK.getCode(), lng, lat, 500)
    );

    // 병원 검색 (500m)
    Mono<KakaoCategorySearchResponse> hospital = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.HOSPITAL.getCode(), lng, lat, 500)
    );

    return Mono.zip(convenienceStore, subway, bank, hospital)
            .map(tuple -> {
              Map<String, Object> result = new HashMap<>();

              // 편의점 정보
              result.put("편의점", Map.of(
                      "개수", tuple.getT1().documents().size(),
                      "가장 가까운 곳", tuple.getT1().documents().isEmpty() ? "없음" :
                              Map.of(
                                      "이름", tuple.getT1().documents().get(0).placeName(),
                                      "거리", tuple.getT1().documents().get(0).distance() + "m"
                              )
              ));

              // 지하철역 정보
              result.put("지하철역", Map.of(
                      "개수", tuple.getT2().documents().size(),
                      "가장 가까운 곳", tuple.getT2().documents().isEmpty() ? "없음" :
                              Map.of(
                                      "이름", tuple.getT2().documents().get(0).placeName(),
                                      "거리", tuple.getT2().documents().get(0).distance() + "m"
                              )
              ));

              // 은행 정보
              result.put("은행", Map.of(
                      "개수", tuple.getT3().documents().size(),
                      "가장 가까운 곳", tuple.getT3().documents().isEmpty() ? "없음" :
                              Map.of(
                                      "이름", tuple.getT3().documents().get(0).placeName(),
                                      "거리", tuple.getT3().documents().get(0).distance() + "m"
                              )
              ));

              // 병원 정보
              result.put("병원", Map.of(
                      "개수", tuple.getT4().documents().size(),
                      "가장 가까운 곳", tuple.getT4().documents().isEmpty() ? "없음" :
                              Map.of(
                                      "이름", tuple.getT4().documents().get(0).placeName(),
                                      "거리", tuple.getT4().documents().get(0).distance() + "m"
                              )
              ));

              return result;
            });
  }

  @Operation(summary = "카테고리 코드 목록", description = "사용 가능한 카테고리 코드 목록을 반환합니다.")
  @GetMapping("/categories")
  public Map<String, String> getCategories() {
    Map<String, String> categories = new HashMap<>();
    for (KakaoCategoryCode code : KakaoCategoryCode.values()) {
      categories.put(code.getCode(), code.getDescription());
    }
    return categories;
  }
}
