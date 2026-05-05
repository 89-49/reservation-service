# 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# dos2unix 설치 (줄바꿈 문자 변환 도구)
RUN apk add --no-cache dos2unix

# 소스 코드 복사
COPY . .

# gradlew 파일의 줄바꿈 문자를 CRLF -> LF로 변환 및 권한 부여
RUN dos2unix gradlew
RUN chmod +x ./gradlew

# 빌드 실행 (인증 정보 주입 필요 - 아래 설명 참고)
RUN ./gradlew clean bootJar -x test

# 실행 단계
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 보안을 위해 실행 단계에서 사용자 생성
RUN addgroup -S appuser && adduser -S appuser -G appuser

# 빌드 결과물 복사 (plain.jar를 제외하고 딱 하나만 복사되도록 명시)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
# 또는 명확하게 하나만 남도록 빌드 설정이 되어있다면 그대로 사용 가능합니다.

# SSL 인증서 폴더 생성 및 복사
RUN mkdir -p /app/ssl
# 파일이 있을 때만 복사하도록 구성하거나, 확실히 존재해야 함을 명시
COPY src/main/resources/ssl/*.jks /app/ssl/

# 권한 설정 (사용자 생성 후 일괄 변경)
RUN chown -R appuser:appuser /app

# 환경 변수 및 포트 설정
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8085

USER appuser

# 최적화된 Java 실행 옵션 (Container 환경 인식)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]