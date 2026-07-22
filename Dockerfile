# =========================================================
#  백엔드(Spring Boot) 멀티스테이지 빌드
#  build 스테이지: JDK로 bootJar 생성 (테스트 제외 — 테스트는 CI에서)
#  run 스테이지: JRE만 담아 이미지 경량화
# =========================================================

# --- build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 캐시 레이어 (gradle 파일 먼저 복사 → 소스만 바뀌면 의존성 재다운 안 함)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# --- run ---
# alpine: 이미지 경량(1GB VM 유리) + busybox wget 내장(compose healthcheck용)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 1GB VM이라 힙 상한을 둔다 (compose에서 JAVA_OPTS로 덮어쓸 수 있음)
ENV JAVA_OPTS="-Xmx350m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
