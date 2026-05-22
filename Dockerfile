## Stage 1 : build Maven
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /code
COPY . .
RUN mvn package -DskipTests -q

## Stage 2 : runtime
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /code/target/quarkus-app/lib/ /app/lib/
COPY --from=build /code/target/quarkus-app/*.jar /app/
COPY --from=build /code/target/quarkus-app/app/ /app/app/
COPY --from=build /code/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080
USER 1001
CMD ["java", "-Xmx256m", "-jar", "quarkus-run.jar"]