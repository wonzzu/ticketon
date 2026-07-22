# =========================================================
#  프론트(Vue) 빌드 → Nginx가 정적 서빙 + /api 프록시
#  이 이미지 하나가 "프론트 서버 + 리버스 프록시" 역할
# =========================================================

# --- build: Vue dist 생성 ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# .env.production의 VITE_API_BASE_URL=/api 로 빌드됨
RUN npm run build

# --- serve: Nginx ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
