# imjang

> 부동산 현장 방문 기록 웹앱 — 지도 마커, 체크리스트, 타임라인

[![Live](https://img.shields.io/badge/live-ddangkun.cloud-16a34a?style=flat-square&logo=vercel&logoColor=white)](https://ddangkun.cloud)
[![API](https://img.shields.io/badge/api-api.ddangkun.cloud-0f766e?style=flat-square&logo=spring&logoColor=white)](https://api.ddangkun.cloud/actuator/health)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-passing-16a34a?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/marista-dev/imjang-backend/actions)

---

![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java_21-000000?style=flat-square&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-336791?style=flat-square&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Oracle Cloud](https://img.shields.io/badge/Oracle_Cloud-F80000?style=flat-square&logo=oracle&logoColor=white)

---

## Stack

| | |
|:---|:---|
| **Backend** | Spring Boot 3.5 · Java 21 · Gradle |
| **Database** | PostgreSQL 16 · Docker Compose |
| **Auth** | Session-based · Email OTP |
| **Map** | Kakao Maps JS SDK (geocoding) |

## Infrastructure

| | |
|:---|:---|
| **Host** | Oracle Cloud Free Tier · A1.Flex · arm64 · Ubuntu 22.04 |
| **Proxy** | Nginx · Let's Encrypt SSL · `api.ddangkun.cloud` |
| **CI/CD** | GitHub Actions → GHCR → Docker pull |
| **Frontend** | Vercel · `ddangkun.cloud` |

## Quick Start

```bash
./gradlew bootRun
```

```bash
# health check
curl https://api.ddangkun.cloud/actuator/health
# {"status":"UP"}
```

---

<sub>Solo project · marista-dev · 2025–2026 · MIT</sub>
