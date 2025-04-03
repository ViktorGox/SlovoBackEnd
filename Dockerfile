FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/slovoBackend.jar slovoBackend.jar

EXPOSE 8080

CMD ["java", "-jar", "slovoBackend.jar"]
