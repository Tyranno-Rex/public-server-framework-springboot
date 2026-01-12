# DDIP Spring Boot Backend

## 개요
DDIP 프로젝트의 Spring Boot 백엔드 애플리케이션입니다. PostgreSQL을 사용하여 게시물 데이터를 관리합니다.

## 기술 스택
- Spring Boot 3.5.0
- Spring Data JPA
- PostgreSQL
- Lombok
- Spring Security
- JWT (JSON Web Token) - 인증/인가
- Springdoc OpenAPI 2.8.5 - API 문서화

## 설정

### 1. PostgreSQL 설치 및 설정
```bash
# macOS (Homebrew 사용)
brew install postgresql
brew services start postgresql

# 데이터베이스 생성
createdb ddip

# 또는 psql을 통해 생성
psql postgres
CREATE DATABASE ddip;
\q
```

### 2. 애플리케이션 설정
`src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 확인하세요:
- 데이터베이스 URL: `jdbc:postgresql://localhost:5432/ddip`
- 사용자명: `postgres`
- 비밀번호: `password`

필요에 따라 이 값들을 수정하세요.

## 실행 방법

### 1. Gradle을 사용한 실행
```bash
cd back/ddip-spring
./gradlew bootRun
```

### 2. IDE에서 실행
`Application.java` 파일을 실행하세요.

## API 문서

### Swagger UI
애플리케이션 실행 후 다음 URL에서 대화형 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

Swagger UI에서 다음 기능을 사용할 수 있습니다:
1. 모든 API 엔드포인트 조회
2. 요청/응답 스키마 확인
3. API 직접 테스트 (Try it out)
4. JWT 토큰 인증 설정 (Authorize 버튼)

## API 엔드포인트

### 인증 API (`/api/auth`)
- `POST /api/auth/login` - 카카오 로그인 (JWT 토큰 발급)
- `POST /api/auth/refresh` - Access Token 갱신
- `POST /api/auth/logout` - 로그아웃 (Refresh Token 삭제)
- `GET /api/auth/me` - 현재 사용자 정보 조회
- `GET /api/auth/health` - 헬스체크

### 게시물 관련 API
- `GET /api/posts` - 모든 게시물 조회
- `GET /api/posts/{id}` - 특정 게시물 조회
- `POST /api/posts` - 새 게시물 생성
- `PUT /api/posts/{id}` - 게시물 수정
- `DELETE /api/posts/{id}` - 게시물 삭제

### 인증 방법
1. `/api/auth/login`으로 카카오 Access Token을 전송하여 JWT 토큰 발급
2. 발급받은 Access Token을 `Authorization: Bearer {token}` 헤더에 포함
3. Access Token 만료 시 `/api/auth/refresh`로 새 토큰 발급

### POST /api/auth/login 요청 예시
```json
{
  "kakaoAccessToken": "카카오 Access Token"
}
```

### POST /api/posts 요청 예시
```json
{
  "title": "게시물 제목",
  "content": "게시물 내용입니다.",
  "authorId": "user123",
  "authorName": "사용자명"
}
```

## 데이터베이스 스키마
JPA가 자동으로 `posts` 테이블을 생성합니다:
- `id` (BIGINT, PRIMARY KEY)
- `content` (TEXT, NOT NULL)
- `author_id` (VARCHAR, NOT NULL)
- `author_name` (VARCHAR, NOT NULL)
- `likes` (INTEGER, DEFAULT 0)
- `comments` (INTEGER, DEFAULT 0)
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP)

## CORS 설정
Flutter 앱과의 연동을 위해 다음 도메인에서의 요청을 허용합니다:
- `http://localhost:3000`
- `http://localhost:8081` 