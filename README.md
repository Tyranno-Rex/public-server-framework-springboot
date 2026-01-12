# Public Server Framework

Production-ready Spring Boot 3.5.0 server framework with Java 21.

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 (LTS) + Virtual Threads |
| **Framework** | Spring Boot 3.5.0 |
| **Database** | PostgreSQL (primary), MongoDB (optional) |
| **Cache** | Redis |
| **API** | REST + gRPC |
| **Real-time** | WebSocket/STOMP |
| **Auth** | JWT (Access/Refresh Token) |
| **Docs** | Swagger/OpenAPI 3.0 |

## Features

### Core
- Unified API Response (`ApiResponse<T>`)
- Base Entity with JPA Auditing
- Global Exception Handling
- Request/Response Logging
- Health Check API

### Security
- JWT Authentication (Access/Refresh Token)
- Role-based Access Control
- Rate Limiting (Bucket4j)
- Environment-specific Security Config

### Performance
- Virtual Threads (Java 21)
- Redis Caching
- HikariCP Connection Pool
- Hibernate Batch Processing
- HTTP Response Compression

### Resilience
- Circuit Breaker (Resilience4j)
- Retry Pattern
- Distributed Lock (Redis)
- Graceful Shutdown

### Observability
- MDC Logging (TraceId/SpanId)
- Request Tracing
- Audit Log (AOP)
- Actuator Endpoints

### Communication
- gRPC Server with Interceptors
- WebSocket/STOMP (optional)
- WebClient for External APIs

### Optional Features
- MongoDB Support (conditional)
- WebSocket Real-time (conditional)
- Domain Event Publishing

## Quick Start

### Prerequisites
- Java 21 (auto-downloaded via Gradle Toolchain)
- Docker & Docker Compose
- Git

### 1. Clone & Setup
```bash
git clone <repository-url>
cd public-server-framework
cp .env.example .env
# Edit .env with your settings
```

### 2. Start Infrastructure
```bash
docker-compose up -d
```

### 3. Run Application
```bash
# Development
./gradlew bootRun

# Or with specific profile
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### 4. Access
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/api/health
- **gRPC**: localhost:9090

## Project Structure

```
src/main/java/com/common/server/
├── api/                    # REST Controllers
│   ├── v1/                 # API Version 1
│   └── v2/                 # API Version 2
├── grpc/                   # gRPC Services
│   ├── interceptor/        # gRPC Interceptors
│   └── service/            # gRPC Service Implementations
├── common/                 # Common Utilities
│   ├── config/             # Configuration Classes
│   ├── dto/                # Data Transfer Objects
│   ├── entity/             # JPA Entities
│   ├── exception/          # Exception Handling
│   ├── logging/            # Logging Components
│   ├── resilience/         # Resilience4j Config
│   ├── security/           # Security Config
│   └── util/               # Utility Classes
├── mongodb/                # MongoDB Support (optional)
├── websocket/              # WebSocket Support (optional)
└── Application.java        # Main Application
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/test/prod) | dev |
| `DB_URL` | PostgreSQL URL | jdbc:postgresql://localhost:5432/server |
| `DB_USERNAME` | DB username | postgres |
| `DB_PASSWORD` | DB password | password |
| `JWT_SECRET` | JWT signing key | (required) |
| `SPRING_DATA_REDIS_HOST` | Redis host | localhost |
| `SPRING_DATA_REDIS_PORT` | Redis port | 6379 |
| `GRPC_SERVER_PORT` | gRPC server port | 9090 |
| `MONGODB_ENABLED` | Enable MongoDB | false |
| `WEBSOCKET_ENABLED` | Enable WebSocket | false |
| `VIRTUAL_THREADS_ENABLED` | Enable Virtual Threads | false |

### Profiles

- **dev**: Development mode, Swagger enabled, detailed logging
- **test**: Testing mode, in-memory database options
- **prod**: Production mode, Swagger disabled, optimized settings

## API Documentation

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

### API Versioning
APIs are versioned via URL prefix:
- `/api/v1/*` - Version 1
- `/api/v2/*` - Version 2

### Authentication
1. Login via `/api/v1/auth/login` to get JWT tokens
2. Include Access Token in requests:
   ```
   Authorization: Bearer <access_token>
   ```
3. Refresh tokens via `/api/v1/auth/refresh`

### Standard Response Format
```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2025-01-13T12:00:00"
}
```

## gRPC

### Service Definition
Proto files are located in `src/main/proto/`.

### Client Connection
```java
ManagedChannel channel = ManagedChannelBuilder
    .forAddress("localhost", 9090)
    .usePlaintext()
    .build();
```

### Testing with grpcurl
```bash
# List services
grpcurl -plaintext localhost:9090 list

# Call method
grpcurl -plaintext -d '{"name": "test"}' localhost:9090 example.Service/Method
```

## WebSocket (Optional)

Enable in application.properties:
```properties
websocket.enabled=true
```

### Endpoints
- **SockJS**: `/ws`
- **Native WebSocket**: `/ws-native`

### Topics
- `/topic/chat/{roomId}` - Chat rooms
- `/topic/announcements` - Broadcast
- `/user/queue/notifications` - Personal notifications

### Client Example
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    stompClient.subscribe('/topic/chat/room1', (message) => {
        console.log(JSON.parse(message.body));
    });
});
```

## MongoDB (Optional)

Enable in application.properties:
```properties
spring.data.mongodb.enabled=true
spring.data.mongodb.uri=mongodb://localhost:27017/server
```

Use for:
- Chat messages
- Activity logs
- Unstructured data

## Distributed Lock

### Annotation-based
```java
@DistributedLock(key = "'order:' + #orderId", waitTime = 5, leaseTime = 10)
public void processOrder(String orderId) {
    // Critical section
}
```

### Programmatic
```java
@Autowired
private DistributedLockService lockService;

lockService.executeWithLock("resource:123", 5, 10, TimeUnit.SECONDS, () -> {
    // Critical section
    return result;
});
```

## Circuit Breaker

### Pre-configured Circuit Breakers
- `externalApi` - For external API calls
- `payment` - For payment processing

### Usage
```java
@CircuitBreaker(name = "externalApi", fallbackMethod = "fallback")
@Retry(name = "externalApi")
public Response callExternalService() {
    // External call
}

public Response fallback(Exception e) {
    return Response.fallback();
}
```

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## Build & Deploy

### Build JAR
```bash
./gradlew clean build
```

### Docker Build
```bash
docker build -t public-server-framework .
```

### Docker Run
```bash
docker run -d \
  --name server \
  -p 8080:8080 \
  -p 9090:9090 \
  -e JWT_SECRET=your-secret \
  -e DB_URL=jdbc:postgresql://host:5432/db \
  public-server-framework
```

## Monitoring & Observability

### Docker Compose Profiles

```bash
# 기본 인프라만 (PostgreSQL, Redis)
docker-compose up -d

# 모니터링 포함 (Prometheus, Grafana, Alertmanager)
docker-compose --profile monitoring up -d

# ELK 스택 포함 (Elasticsearch, Logstash, Kibana)
docker-compose --profile elk up -d

# 전체 스택
docker-compose --profile full up -d
```

### 모니터링 UI 접속

| Service | URL | Credentials |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9091 | - |
| **Kibana** | http://localhost:5601 | - |
| **Alertmanager** | http://localhost:9093 | - |

### Prometheus Metrics

Spring Boot Actuator + Micrometer를 통해 다음 메트릭을 수집합니다:

- **HTTP 요청**: 요청 수, 응답 시간, 상태 코드별 분포
- **JVM**: 힙 메모리, GC, 스레드
- **HikariCP**: 커넥션 풀 상태
- **Resilience4j**: Circuit Breaker 상태

```bash
# Prometheus 메트릭 엔드포인트
curl http://localhost:8080/actuator/prometheus
```

### Grafana 대시보드

사전 구성된 대시보드:
- **Spring Boot Application**: JVM, HTTP, DB 커넥션 풀 모니터링
- 추가 대시보드는 `docker/grafana/dashboards/`에 JSON으로 추가

### ELK 스택 (로그 수집)

프로덕션 환경에서 로그가 자동으로 Logstash → Elasticsearch로 전송됩니다.

```bash
# Kibana에서 인덱스 패턴 생성
# Index pattern: server-logs-*
# Time field: @timestamp
```

### 알림 설정

`docker/alertmanager/alertmanager.yml`에서 알림 수신자 설정:
- Slack
- Email
- PagerDuty

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

### Logging

Logs include TraceId and SpanId for request tracing:
```
2025-01-13 12:00:00 [traceId=abc123, spanId=def456] INFO ...
```

## License

MIT License
