FROM maven:3.9.16-eclipse-temurin-25-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
# Os testes unitários rodam na imagem; os de integração (Testcontainers) e o limite de cobertura do JaCoCo,
# medido com unitários + integração, rodam no CI e nos gates.
RUN mvn -B -ntp -DskipITs -Djacoco.skip=true verify

FROM eclipse-temurin:25.0.4_7-jre-alpine-3.24
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/kanban-2.0.1.jar app.jar
USER app:app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
