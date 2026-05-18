# reservation-service

예약 생명주기를 담당하는 Spring Boot 기반 마이크로서비스입니다.대량의 동시 예약 신청 트래픽을 제어하기 위해 Redisson 분산 락 아키텍처를 도입했으며,예약 성공 및 상태 변경 시 아웃박스(Outbox) 패턴을 통해 이벤트를 발행합니다.

## 주요 기능

- 상품 생성 Kafka 이벤트 수신 후 예약 자동 생성
- 예약 목록/단건 상세 조회 REST API 제공
- 분산 락(Redisson) 기반 안전한 동시 예약 신청 제어
- 구매자/판매자 상호 예약 확정 및 취소 처리
- 트랜잭션 보장(Outbox 패턴)을 통한 예약 완료/취소/실패 이벤트 발행
- Promtail 호환 설정을 통한 분산 로그 수집

## 기술 스택

- Java 21
- Spring Boot 3.5.13
- Spring Cloud 2025.0.2
- Spring Data JPA
- Querydsl 6.8
- Spring Data Redis & Redisson
- Spring Kafka
- Eureka Client
- Spring Security & JWT
- PostgreSQL
- Docker, Docker Compose
- Grafana Alloy (Promtail config format)

## 프로젝트 구조

```text
src/main/java/org/pgsg/reservation
├── application      # 유스케이스, 서비스, 포트, 커맨드/결과 DTO
├── domain           # 예약 도메인 모델, 값 객체, 도메인 예외
├── infrastructure   # JPA 레포지토리 구현체, Kafka, Redis 인프라 어댑터
└── presentation     # REST 컨트롤러, API 요청/응답 DTO, 분산 락 제어 패사드
```

## 사전 준비

- JDK 21
- Docker, Docker Compose
- GitHub Package Registry 접근 권한
- 실행 환경에 맞는 Config Server, Eureka, Kafka, DB 설정

`org.pgsg:common` 패키지를 GitHub Package Registry에서 내려받기 때문에 Gradle 인증 정보가 필요합니다.

```properties
# ~/.gradle/gradle.properties
gpr.user=GITHUB_USERNAME
gpr.token=GITHUB_TOKEN
```

또는 환경변수로 지정할 수 있습니다.

```bash
export GPR_USER=GITHUB_USERNAME
export GPR_TOKEN=GITHUB_TOKEN
```

## 설정

애플리케이션은 기본적으로 `.env`, Config Server, Eureka를 사용합니다.

```yaml
spring:
  application:
    name: reservation-service
  profiles:
    active: kafka, topics, dev
  config:
    import:
      - "optional:file:.env[.properties]"
      - "optional:configserver:"
```

로컬 또는 배포 환경에서는 다음 값이 필요합니다.

| 변수 | 설명 |
| --- | --- |
| `SERVER_IP` | Eureka 서버 URL 목록 |
| `SERVER_PORT` | 외부 포트 바인딩용 변수  |
| `DB_NAME` | 연결 및 생성 대상 PostgreSQL 데이터베이스 이름 |
| `DB_USERNAME` | PostgreSQL 데이터베이스 사용자명 |
| `DB_PASSWORD` | PostgreSQL 데이터베이스 접근 비밀번호 |
| `REDIS_HOST` | 분산 락 및 캐시용 Redis 컨테이너 접속 호스트명 |
| `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` | 트레이싱 수집용 외부 Zipkin API 엔드포인트 URL |
| `KAFKA_SSL_PATH` | 컨테이너 내부에 마운트되는 Kafka SSL truststore 파일 경로 |
| `AR_IMAGE_PATH` | Google Artifact Registry 등 도커 이미지 배포 저장소 경로 |
| `IMAGE_TAG` | 도커 이미지 빌드 및 배포용 태그 버전 |
| `GPR_USER` | GitHub Package Registry 사용자명 |
| `GPR_TOKEN` | GitHub Package Registry 토큰 |

Kafka topic 설정은 Config Server 또는 활성 프로필 설정에서 제공되어야 합니다.

| 설정 키 | 설명 |
| --- | --- |
| `prod-reservation-completed` | 예약 완료 이벤트 발행 topic |
| `prod-reservation-cancelled` | 예약 취소 이벤트 발행 topic |
| `prod-reservation-tradefail` | 거래 실패로 인한 예약 실패 처리 수신 topic |
| `prod-product-created` | 상품 생성 이벤트 수신 topic |
| `prod-product-failed` | 상품 생성 실패 이벤트 수신 topic |

## Docker 실행

로컬 Docker Compose는 `trade-service`와 로그 수집용 Alloy 컨테이너를 함께 실행합니다.

```bash
docker compose up --build
```

기본 포트 매핑은 다음과 같습니다.

| 대상 | 포트 |
| --- | --- |
| Host | `19020` |
| Container | `8085` |

Compose 실행 전 외부 네트워크가 필요합니다.

```bash
docker network create pgsg-network
```

Docker 빌드 시 `~/.gradle/gradle.properties`가 BuildKit secret으로 전달됩니다.

## API

### 예약 생성

```http
POST /api/v1/reservations
```

### 예약 목록 조회

```http
GET /api/v1/reservations?page=0&size=10
```
- page: 0 이상의 페이지 번호
- size: 1 이상의 페이지 크기 (기본값: 10)
- sellerName: 판매자 이름 필터 (선택)
- buyerName: 구매자 이름 필터 (선택)
- productName: 상품명 필터 (선택)
- status: 예약 상태 필터 (선택)
- productId: 상품 ID 필터 (선택)

### 예약 상세 목록 조회

```http
GET /api/v1/reservations/{reservationId}
```

### 예약 신청

```http
PATCH /api/v1/reservations/{reservationId}
```

### 구매자 사유 취소

```http
PATCH /api/v1/reservations/{reservationId}/cancel/buyer
```

### 결제 완료

```http
PATCH /api/v1/reservations/{reservationId}/paymentconfirm
```

### 판매자 사유 취소

```http
PATCH /api/v1/reservations/{reservationId}/cancel/seller
```

### 예약 만료 (관리자)

```http
PATCH /api/v1/reservations/{reservationId}/expire
```

### 예약 완료

```http
PATCH /api/v1/reservations/{reservationId}/complete
```

### 거래 완료
```http
PATCH /api/v1/reservations/{reservationId}/tradeconfirm
```

예약 및 거래 상태 관리 로직
- 인증 사용자 기반 상태 저장: Bearer Token을 이용해 요청한 현재 사용자의 권한을 검증하고,해당 예약건에 대한 구매자 또는 판매자의 개별 상태를 저장합니다.

최종 예 완료 및 이벤트 발행 조건 (COMPLETED):
- 구매자의 결제 완료(paymentconfirm)와 판매자의 채팅 수락(complete) 두 개의 상호 확정이 모두 이루어져야 합니다.
- 구매자와 판매자가 모두 예약 완료 처리를 마치는 시점에 최종적으로 거래 상태가 COMPLETED로 전환되며, 외부 서비스 전파를 위한 예약 완료 이벤트 발행이 요청됩니다.

## 이벤트 흐름

### 수신 이벤트

`prod-product-created` topic의 상품 타임 이벤트를 수신해 예약를 생성합니다.

```json
{
	"productId": "00000000-0000-0000-0000-000000000000",
	"name": "상품명",
	"price": 10000,
	"endTime": [ 2026,5,21,12,24,41 ],
	"sellerId": "00000000-0000-0000-0000-000000000000",
	"sellerName": "판매자"
}
```

`prod-trade-completed` topic의 거래 완료 이벤트를 수신해 예약은 완료합니다.
```json
{
	"tradeId": "00000000-0000-0000-0000-000000000000",
	"reservationId": "00000000-0000-0000-0000-000000000000",
  "productId": "00000000-0000-0000-0000-000000000000"
}
```

### 발행 이벤트

서비스는 common 모듈의 Outbox 이벤트를 통해 다음 이벤트 등록을 요청합니다.

- 예약 완료: prod-reservation-completed
- 예약 취소: prod-reservation-cancelled
- 예약 실패: prod-reservation-tradefail
  
## 로그 수집

`promtail-config.yml`과 `deploy/promtail-config.yml`은 `/logs/*.log` 파일을 수집해 Loki 엔드포인트로 전송합니다. 로컬 Compose와 운영 Compose 모두 Grafana Alloy를 Promtail config format으로 실행합니다.

## 배포

운영 Compose 파일은 `deploy/docker-compose.prod.yaml`에 있습니다.

```bash
docker compose -f deploy/docker-compose.prod.yaml up -d
```

운영 환경에서는 다음 경로를 사용합니다.

| 경로 | 용도 |
| --- | --- |
| `/opt/reservation-service/ssl` | Kafka SSL 인증서 |
| `/opt/reservation-service/logs` | 애플리케이션 로그 |
| `/opt/reservation-service/promtail-config.yml` | 로그 수집 설정 |

## 테스트

```bash
./gradlew test
```

테스트는 도메인 모델, 애플리케이션 서비스, 영속성 어댑터, Kafka Consumer, 컨트롤러, 아키텍처 규칙을 검증합니다.
