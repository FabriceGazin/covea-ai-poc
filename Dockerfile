## Stage 1 : build Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /code
# Dépendances d'abord (cache Docker)
COPY pom.xml .
RUN mvn -B dependency:go-offline -q
# Source
COPY src ./src
RUN mvn package -DskipTests -q

## Stage 2 : runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /code/target/quarkus-app/lib/ /app/lib/
COPY --from=build /code/target/quarkus-app/*.jar /app/
COPY --from=build /code/target/quarkus-app/app/ /app/app/
COPY --from=build /code/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080
USER 1001
CMD ["java", "-Xmx256m", "-jar", "quarkus-run.jar"]