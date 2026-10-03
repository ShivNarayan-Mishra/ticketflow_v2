# Stage 1: build the jar using Maven (this stage's image is thrown away after)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# Stage 2: run it with just a JRE, not the full Maven+JDK toolchain -
# keeps the final image smaller since it doesn't need to compile anything
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/booking-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]