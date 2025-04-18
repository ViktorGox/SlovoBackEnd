FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY uploads/gp_default.png ./uploads/gp_default.png
COPY uploads/pf_default.png ./uploads/pf_default.png
COPY --from=builder /app/build/libs/slovoBackend.jar slovoBackend.jar

EXPOSE 8080

CMD ["java", "-jar", "slovoBackend.jar"]
