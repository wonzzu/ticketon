# 🎫 Ticketing

> 콘서트 · 공연 예매 플랫폼

## 스택

- **Backend**: Spring Boot 3.4 · JPA · QueryDSL · MySQL · Redis · JWT · Spring Security
- **Frontend**: Vue 3 · Vite · Pinia · Vue Router · Axios · Bootstrap 5.3

## 실행

### Backend (port 8080)
```bash
./gradlew bootRun
```

### Frontend (port 5173)
```bash
cd frontend
npm install
npm run dev
```

## 환경 변수

`application-local.properties` 또는 환경 변수로 주입:

| Key | 설명 |
|-----|------|
| `DB_USERNAME` | MySQL 사용자 (기본: root) |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상) |

---

<!-- TODO: 추후 추가 -->
<!-- - 도메인 구조 / ERD -->
<!-- - API 명세 -->
<!-- - 화면 스크린샷 -->
<!-- - 대기열 / 좌석 선점 / 결제 흐름 다이어그램 -->
