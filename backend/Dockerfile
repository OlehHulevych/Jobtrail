FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon && rm -f build/libs/*-plain.jar

FROM eclipse-temurin:25-jre
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
