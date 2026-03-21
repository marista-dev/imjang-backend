# 백엔드 수정/구현 사항

프론트엔드 전체 플로우 QA에서 발견된 백엔드 이슈.
우선순위 순으로 정렬.

---

## B-001: 매물 상세 API images 필드에 이미지 ID 추가 (GET /properties/:id/detail)

### 현재 상황
`PropertyDetailResponse`의 `images` 필드가 **문자열 URL 배열**만 반환:
```json
"images": ["/temp-images/user2/2026/03/thumb_xxx.png"]
```

### 문제
프론트엔드에서 이미지 삭제 시 `DELETE /properties/:id/images/:imageId` 호출이 필요하지만,
detail API 응답에 이미지 ID가 없어서 삭제 API를 호출할 수 없음.
현재 프론트에서 배열 인덱스(0, 1, 2...)를 ID로 사용 → 400 에러 발생.

### 요청 사항
`PropertyDetailResponse`의 images 필드를 **객체 배열**로 변경:

AS-IS:
```java
@Schema(description = "이미지 URL 목록")
List<String> images
```

TO-BE:
```java
@Schema(description = "이미지 목록")
List<PropertyImageDto> images

// 새 DTO
public record PropertyImageDto(
    @Schema(description = "이미지 ID", example = "42")
    Long imageId,
    
    @Schema(description = "썸네일 URL")
    String thumbnailUrl,
    
    @Schema(description = "원본 URL")
    String originalUrl
) {}
```

### 기대 응답
```json
"images": [
  { "imageId": 42, "thumbnailUrl": "/temp-images/.../thumb_xxx.png", "originalUrl": "/images/.../xxx.png" },
  { "imageId": 43, "thumbnailUrl": "/temp-images/.../thumb_yyy.png", "originalUrl": "/images/.../yyy.png" }
]
```

### 영향 범위
- `PropertyDetailResponse` 수정
- `PropertyDetailService` (또는 해당 서비스)에서 이미지 조회 시 ID 포함
- 프론트엔드에서 `normalizeProperty()` 유틸 수정 (이미지 ID 직접 사용)

---

## B-002: 지도 마커 API 구현 (GET /properties/map/markers)

### 현재 상황
프론트엔드 지도 페이지가 완성되어 있지만, 백엔드 마커 API가 아직 미구현.
현재 빈 배열 `{"markers":[]}` 만 반환.

---

### API 스펙

**엔드포인트**: `GET /api/v1/properties/map/markers`
**인증**: `@LoginRequired` (세션 기반)

**요청 파라미터** (Query String, 모두 필수):

| 파라미터 | 타입 | 설명 | 예시 |
|---|---|---|---|
| `southWestLat` | Double | 뷰포트 남서쪽 위도 | 37.546 |
| `southWestLng` | Double | 뷰포트 남서쪽 경도 | 127.059 |
| `northEastLat` | Double | 뷰포트 북동쪽 위도 | 37.576 |
| `northEastLng` | Double | 뷰포트 북동쪽 경도 | 127.076 |
| `zoomLevel` | Integer | 카카오맵 줌 레벨 (1~14) | 5 |

**응답 본문** (200 OK):
```json
{
  "markers": [
    {
      "id": 17,
      "latitude": 37.5612,
      "longitude": 127.0345,
      "address": "서울 성동구 가람길 287",
      "priceType": "JEONSE",
      "deposit": 11111,
      "monthlyRent": null,
      "price": null,
      "rating": 5,
      "thumbnailUrl": "/temp-images/user2/2026/03/thumb_xxx.png"
    }
  ]
}
```

**응답 DTO:**
```java
public record MapMarkerResponse(
    List<MapMarkerDto> markers
) {}

public record MapMarkerDto(
    @Schema(description = "매물 ID") Long id,
    @Schema(description = "위도") Double latitude,
    @Schema(description = "경도") Double longitude,
    @Schema(description = "주소") String address,
    @Schema(description = "거래유형: JEONSE, MONTHLY_RENT, SALE") String priceType,
    @Schema(description = "보증금/전세금") Long deposit,
    @Schema(description = "월세") Long monthlyRent,
    @Schema(description = "매매가") Long price,
    @Schema(description = "별점 1~5") Integer rating,
    @Schema(description = "썸네일 URL") String thumbnailUrl
) {}
```

**비즈니스 로직:**
1. 현재 로그인된 사용자의 매물만 반환
2. `latitude`/`longitude`가 뷰포트 범위 내인 매물만 필터링:
   - `latitude BETWEEN southWestLat AND northEastLat`
   - `longitude BETWEEN southWestLng AND northEastLng`
3. 좌표가 null인 매물은 제외
4. `zoomLevel`은 향후 클러스터링에 사용 가능 (우선은 무시)

**JPA 쿼리 예시:**
```java
@Query("SELECT p FROM Property p WHERE p.userId = :userId " +
       "AND p.latitude BETWEEN :swLat AND :neLat " +
       "AND p.longitude BETWEEN :swLng AND :neLng " +
       "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
List<Property> findMarkersInBounds(
    @Param("userId") Long userId,
    @Param("swLat") Double swLat,
    @Param("swLng") Double swLng,
    @Param("neLat") Double neLat,
    @Param("neLng") Double neLng
);
```

**참고: summary API는 불필요**
프론트에서 markers 응답 데이터를 그대로 요약 카드 Drawer에 표시.
markers에 위 필드가 모두 포함되면 별도 summary API는 구현하지 않아도 됨.

---

### 선행 조건: 매물 생성 시 좌표 저장 확인

프론트엔드는 매물 등록 시 `latitude`/`longitude`를 payload에 포함해서 전송.

**확인 필요:**
1. `CreatePropertyRequest` DTO에 `latitude` (Double), `longitude` (Double) 필드가 있는지
2. Property 엔티티에 latitude/longitude 컬럼이 있는지
3. `PropertyService.createProperty()`에서 좌표를 실제 DB에 저장하는지

**없으면 추가 필요:**
```java
// CreatePropertyRequest에 추가
@Schema(description = "위도", example = "37.5612")
Double latitude,

@Schema(description = "경도", example = "127.0345")
Double longitude
```

```java
// Property 엔티티에 추가
@Column
private Double latitude;

@Column
private Double longitude;
```

**좌표가 저장되지 않으면 지도 기능 전체가 무용지물.**

---

### 전체 데이터 플로우

```
[매물 등록] → POST /properties (latitude/longitude 포함)
    ↓
[DB에 좌표 저장]
    ↓
[지도 페이지 오픈] → GET /properties/map/markers (viewport bounds)
    ↓
[프론트: 마커 표시] → rating으로 색상 결정 (4+초록, 3노랑, 1~2빨강)
    ↓
[프론트: 필터링] → priceType, rating으로 프론트에서 필터 (API 재호출 없음)
    ↓
[프론트: 마커 클릭] → markers 응답 데이터로 요약 카드 Drawer 표시
    ↓
[프론트: 상세보기] → navigate(`/properties/${id}`)
```
