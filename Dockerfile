# 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# dos2unix 설치
RUN apk add --no-cache dos2unix

# 1. 모든 소스 코드 복사
COPY . .

# 2. 복사된 파일 중 gradlew만 골라서 줄바꿈 변환 및 실행 권한 부여
# (COPY . . 이후에 수행해야 덮어쓰기 문제를 방지할 수 있습니다)
RUN dos2unix gradlew && chmod +x gradlew

# 3. 빌드 실행
RUN ./gradlew clean bootJar -x test

# 실행 단계
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 실행 전용 사용자 생성
RUN addgroup -S appuser && adduser -S appuser -G appuser

# 4. 빌드 결과물 복사
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# 5. SSL 인증서 복사 (물리 경로 확보)
RUN mkdir -p /app/ssl
COPY --from=build /app/src/main/resources/ssl/*.jks /app/ssl/

# 6. 권한 설정
RUN chown -R appuser:appuser /app

# 7. 환경 변수 설정 (Kafka SSL 비밀번호 강제 주입)
ENV SPRING_PROFILES_ACTIVE=dev,kafka
ENV SPRING_KAFKA_SSL_TRUST_STORE_LOCATION=file:/app/ssl/kafka.server.truststore.jks
ENV SPRING_KAFKA_SSL_TRUST_STORE_PASSWORD=_aA123456

EXPOSE 8085
USER appuser

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]