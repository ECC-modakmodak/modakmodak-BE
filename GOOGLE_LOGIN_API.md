# Google OAuth Login - API Documentation

## 새로운 엔드포인트

### Google 로그인

**Endpoint**: `POST /api/users/login/google`

**설명**: Google ID 토큰을 사용하여 로그인합니다. 신규 사용자인 경우 자동으로 회원가입이 진행됩니다.

**Request Body**:
```json
{
  "idToken": "google-id-token-from-frontend"
}
```

**Response (201 Created)**:
```json
{
  "id": 1,
  "message": "로그인 성공",
  "user": {
    "userId": 1,
    "username": "user_google_1234567890",
    "email": "user@gmail.com",
    "nickname": "홍길동",
    "profileImage": "https://lh3.googleusercontent.com/...",
    "provider": "GOOGLE"
  }
}
```

**Error Response (401 Unauthorized)**:
```json
{
  "status": 401,
  "error": "INVALID_TOKEN",
  "message": "Google 로그인 실패: 유효하지 않은 Google ID 토큰입니다."
}
```

## 환경 변수 설정

Google OAuth를 사용하려면 다음 환경 변수를 설정해야 합니다:

```bash
export GOOGLE_CLIENT_ID="your-google-client-id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

또는 `application.yml`에서 직접 설정:
```yaml
google:
  oauth:
    client-id: your-google-client-id
    client-secret: your-google-client-secret
```

## Google Cloud Console 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. 프로젝트 생성 또는 선택
3. "API 및 서비스" > "사용자 인증 정보" 이동
4. "사용자 인증 정보 만들기" > "OAuth 2.0 클라이언트 ID" 선택
5. 애플리케이션 유형: "웹 애플리케이션" 선택
6. 승인된 리디렉션 URI 추가 (프론트엔드 URL)
7. 생성된 클라이언트 ID와 클라이언트 보안 비밀번호를 환경 변수로 설정

## 데이터베이스 마이그레이션

기존 `users` 테이블에 OAuth 필드를 추가해야 합니다:

```sql
ALTER TABLE users 
  ADD COLUMN provider VARCHAR(20) DEFAULT 'LOCAL',
  ADD COLUMN provider_id VARCHAR(255),
  MODIFY COLUMN password VARCHAR(255) NULL;

-- 기존 데이터 업데이트
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL;
```

## 프론트엔드 통합 예시

```javascript
// Google Sign-In 버튼 클릭 시
function handleGoogleLogin() {
  // Google Sign-In SDK를 사용하여 ID 토큰 획득
  const idToken = googleUser.getAuthResponse().id_token;
  
  // 백엔드로 ID 토큰 전송
  fetch('http://localhost:8080/api/users/login/google', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ idToken: idToken })
  })
  .then(response => response.json())
  .then(data => {
    console.log('로그인 성공:', data);
    // 사용자 정보 저장 및 리디렉션
  })
  .catch(error => {
    console.error('로그인 실패:', error);
  });
}
```

## 기존 로컬 로그인과의 호환성

- 기존 로컬 로그인(`POST /api/users/login`)은 그대로 유지됩니다.
- 로컬 로그인 사용자는 `provider="LOCAL"`로 저장됩니다.
- Google 로그인 사용자는 `provider="GOOGLE"`로 저장됩니다.
- 동일한 이메일로 로컬 로그인과 Google 로그인을 별도로 사용할 수 있습니다.
