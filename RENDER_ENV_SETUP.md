# Render 환경 변수 설정 가이드

## Google OAuth 설정

Render 대시보드에서 다음 환경 변수를 추가하세요:

### 1. Google OAuth 인증 정보

```
GOOGLE_CLIENT_ID=여기에-실제-클라이언트-ID-입력
GOOGLE_CLIENT_SECRET=여기에-실제-클라이언트-시크릿-입력
```

### 2. 기존 데이터베이스 설정 (이미 있을 수 있음)

```
SPRING_DATASOURCE_URL=jdbc:mysql://호스트:포트/데이터베이스명
SPRING_DATASOURCE_USERNAME=데이터베이스-사용자명
SPRING_DATASOURCE_PASSWORD=데이터베이스-비밀번호
```

## Render에서 환경 변수 추가하는 방법

1. Render 대시보드 접속 (https://dashboard.render.com/)
2. 해당 서비스(modakmodak-BE) 클릭
3. 왼쪽 메뉴에서 **"Environment"** 탭 클릭
4. **"Add Environment Variable"** 버튼 클릭
5. 위의 환경 변수들을 하나씩 추가:
   - **Key**: `GOOGLE_CLIENT_ID`
   - **Value**: 실제 Google 클라이언트 ID 붙여넣기
   
   - **Key**: `GOOGLE_CLIENT_SECRET`
   - **Value**: 실제 Google 클라이언트 시크릿 붙여넣기

6. **"Save Changes"** 클릭
7. 서비스가 자동으로 재배포됩니다

## 주의사항

- 환경 변수 추가 후 서비스가 자동으로 재시작됩니다
- Google OAuth 설정이 없으면 `/api/users/login/google` 엔드포인트가 작동하지 않습니다
- 기존 로컬 로그인(`/api/users/login`)은 Google OAuth 설정 없이도 정상 작동합니다
