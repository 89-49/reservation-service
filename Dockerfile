# 1. 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 소스 코드 전체 복사 (gradle.properties 포함)
COPY . .

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 빌드 실행 (이미 파일에 인증 정보가 있으므로 추가 인자 없이 실행 가능)
# 만약 빌드 시점에 값을 주입하고 싶다면 그대로 두셔도 무방합니다.
RUN ./gradlew bootJar -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# SSL 인증서 경로 설정 (yml 설정인 /app/ssl/ 경로와 일치시킴)
RUN mkdir -p /app/ssl
COPY src/main/resources/ssl/*.jks /app/ssl/

# 환경 변수 및 포트 설정
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]