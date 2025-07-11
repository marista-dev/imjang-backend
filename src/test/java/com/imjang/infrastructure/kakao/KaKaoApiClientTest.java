package com.imjang.infrastructure.kakao;

import static org.assertj.core.api.Assertions.assertThat;

import com.imjang.infrastructure.kakao.dto.KakaoCategoryCode;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchRequest;
import com.imjang.infrastructure.kakao.dto.KakaoCategorySearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestPropertySource(properties = {
        "kakao.api.key=${KAKAO_API_KEY:test-key}",
        "kakao.api.baseurl=https://dapi.kakao.com"
})
@EnabledIfEnvironmentVariable(named = "KAKAO_API_KEY", matches = ".+")
@DisplayName("카카오 API 통합 테스트 (실제 API 키 필요)")
class KaKaoApiClientTest {

  @Autowired
  private KaKaoApiClient kakaoApiClient;

  private static final Double TEST_LNG = 127.02479803562213;  // 강남역 좌표
  private static final Double TEST_LAT = 37.504585233865086;

  @Test
  @DisplayName("카테고리로 편의점 검색 테스트")
  void searchConvenienceStoreTest() {
    // given
    KakaoCategorySearchRequest request = KakaoCategorySearchRequest.of(
            KakaoCategoryCode.CONVENIENCE_STORE.getCode(),
            TEST_LNG,
            TEST_LAT,
            500  // 500m 반경
    );

    // when
    Mono<KakaoCategorySearchResponse> result = kakaoApiClient.searchByCategory(request);

    // then
    StepVerifier.create(result)
            .assertNext(response -> {
              assertThat(response).isNotNull();
              assertThat(response.meta()).isNotNull(
              );
              assertThat(response.documents()).isNotEmpty();

              // 첫 번째 문서 검증
              KakaoCategorySearchResponse.Document firstDoc = response.documents().get(0);
              assertThat(firstDoc.categoryGroupCode()).isEqualTo("CS2");
              assertThat(firstDoc.placeName()).isNotBlank();
              assertThat(firstDoc.distance()).isNotNull();

              System.out.println("검색된 편의점 수: " + response.documents().size());
              System.out.println("첫 번째 편의점: " + firstDoc.placeName() +
                      " (거리: " + firstDoc.distance() + "m)");
            })
            .verifyComplete();
  }

  @Test
  @DisplayName("카테고리로 지하철역 검색 테스트")
  void searchSubwayStationTest() {
    // given
    KakaoCategorySearchRequest request = KakaoCategorySearchRequest.of(
            KakaoCategoryCode.SUBWAY.getCode(),
            TEST_LNG,
            TEST_LAT,
            1000  // 1km 반경
    );

    // when
    Mono<KakaoCategorySearchResponse> result = kakaoApiClient.searchByCategory(request);

    // then
    StepVerifier.create(result)
            .assertNext(response -> {
              assertThat(response).isNotNull();
              assertThat(response.documents()).isNotEmpty();

              response.documents().forEach(doc -> {
                System.out.println("지하철역: " + doc.placeName() +
                        " (거리: " + doc.distance() + "m)");
              });
            })
            .verifyComplete();
  }

  @Test
  @DisplayName("여러 카테고리 병렬 검색 테스트")
  void searchMultipleCategoriesTest() {
    // given
    Mono<KakaoCategorySearchResponse> convenienceStore = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.CONVENIENCE_STORE.getCode(), TEST_LNG, TEST_LAT, 500)
    );

    Mono<KakaoCategorySearchResponse> subway = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.SUBWAY.getCode(), TEST_LNG, TEST_LAT, 1000)
    );

    Mono<KakaoCategorySearchResponse> bank = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.BANK.getCode(), TEST_LNG, TEST_LAT, 500)
    );

    Mono<KakaoCategorySearchResponse> hospital = kakaoApiClient.searchByCategory(
            KakaoCategorySearchRequest.of(KakaoCategoryCode.HOSPITAL.getCode(), TEST_LNG, TEST_LAT, 500)
    );

    // when & then
    Mono.zip(convenienceStore, subway, bank, hospital)
            .subscribe(tuple -> {
              System.out.println("=== 검색 결과 ===");
              System.out.println("편의점 수: " + tuple.getT1().documents().size());
              System.out.println("지하철역 수: " + tuple.getT2().documents().size());
              System.out.println("은행 수: " + tuple.getT3().documents().size());
              System.out.println("병원 수: " + tuple.getT4().documents().size());
            });
  }

  @Test
  @DisplayName("대형마트 검색 테스트")
  void searchMartTest() {
    // given
    KakaoCategorySearchRequest request = KakaoCategorySearchRequest.of(
            KakaoCategoryCode.MART.getCode(),
            TEST_LNG,
            TEST_LAT,
            2000  // 2km 반경
    );

    // when
    Mono<KakaoCategorySearchResponse> result = kakaoApiClient.searchByCategory(request);

    // then
    StepVerifier.create(result)
            .assertNext(response -> {
              assertThat(response).isNotNull();
              System.out.println("검색된 대형마트 수: " + response.documents().size());

              if (!response.documents().isEmpty()) {
                response.documents().forEach(doc -> {
                  System.out.println("대형마트: " + doc.placeName() +
                          " (거리: " + doc.distance() + "m)");
                });
              }
            })
            .verifyComplete();
  }
}
