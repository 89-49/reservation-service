FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN --mount=type=secret,id=GPR_USER \
    --mount=type=secret,id=GPR_TOKEN \
    GPR_USER=$(cat /run/secrets/GPR_USER) && \
    GPR_TOKEN=$(cat /run/secrets/GPR_TOKEN) && \
    ./gradlew bootJar -x test \
    -Pgpr.user="${GPR_USER}" \
    -Pgpr.token="${GPR_TOKEN}"

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# SSL 인증서 등 리소스 복사
RUN mkdir -p /app/resources
COPY src/main/resources/ssl/*.jks /app/resources/

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]