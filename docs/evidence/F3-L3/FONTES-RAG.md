# F3-L3 — FONTES RAG

Data: 2026-09-23

1. Código do projeto e versões: Spring Boot 3.5.16, Spring Security 6.5.11, Java 25, PostgreSQL 18.6, Compose do projeto.
2. Código-fonte oficial (tags exatas):
   - Spring Boot v3.5.16 — `EndpointRequest` (matchers com `/**`), `ElasticCommonSchemaStructuredLogFormatter` (campos `@timestamp`, `log.level`, `message`, `ecs.version`), `ObservabilityContextCustomizerFactory` (métricas desligadas em teste sem `@AutoConfigureObservability`), `TestRestTemplate.withBasicAuth` (não depreciado): https://github.com/spring-projects/spring-boot/tree/v3.5.16
   - Spring Security 6.5.11 — `HttpSecurity` (`securityMatcher`, `authenticationManager`, `httpBasic`), `DaoAuthenticationProvider(UserDetailsService)`, `InMemoryUserDetailsManager`, `RequestAttributeSecurityContextRepository`: https://github.com/spring-projects/spring-security/tree/6.5.11
   - Docker Compose — injeção de secrets com origem `environment` (modo `0444`, erro se a variável não existir): https://github.com/docker/compose/blob/main/pkg/compose/secrets.go
   - Grafana v13.1.3 — `conf/defaults.ini` (`admin_password = admin`, analytics), exemplos de provisioning, rotas `/api/health`, `/api/datasources/uid/:uid/health`, `/api/dashboards/uid/:uid`: https://github.com/grafana/grafana/tree/v13.1.3
3. Documentação oficial:
   - Javadoc Spring Boot 3.5 `AutoConfigureObservability`: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/test/autoconfigure/actuate/observability/AutoConfigureObservability.html
   - Javadoc Spring Boot 3.5 `EndpointRequest`: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/actuate/autoconfigure/security/servlet/EndpointRequest.html
   - Compose file reference — secrets: https://docs.docker.com/reference/compose-file/secrets/
   - Docker Hub (tags): https://hub.docker.com/v2/repositories/prom/prometheus/tags/v3.14.0 e https://hub.docker.com/v2/repositories/grafana/grafana/tags/13.1.3
   - Situação da API legada de dashboards no Grafana 12/13 (ainda funcional, depreciada): https://github.com/grafana/skills/issues/98
4. Execução local: binário oficial Prometheus 3.14.0 (`promtool check config` e servidor real), javac JDK 21 sem dependências, Biome/TypeScript/Vitest/Vite.
