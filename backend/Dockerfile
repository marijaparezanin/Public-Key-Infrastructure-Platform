FROM bellsoft/liberica-openjdk-alpine-musl:17 AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:resolve

COPY src src
RUN ./mvnw package -DskipTests

FROM bellsoft/liberica-openjdk-alpine-musl:17
WORKDIR /app

COPY .env .env

COPY src/main/resources/ssl/keystore_server.p12 src/main/resources/ssl/keystore_server.p12

COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
