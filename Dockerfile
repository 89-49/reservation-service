FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ARG GPR_USER
ARG GPR_TOKEN

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test \
    -Pgpr.user=${GPR_USER} \
    -Pgpr.token=${GPR_TOKEN}

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/resources
COPY src/main/resources/ssl/*.jks /app/resources/

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]