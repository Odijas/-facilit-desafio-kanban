package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Sessão autenticada para os testes de API: login com CSRF, cookie de sessão e token para escritas.
 */
final class ApiSession {

    private final TestRestTemplate restTemplate;
    private final String baseUrl;
    private final String cookieHeader;
    private final String csrfToken;

    private ApiSession(TestRestTemplate restTemplate, String baseUrl, String cookieHeader, String csrfToken) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.cookieHeader = cookieHeader;
        this.csrfToken = csrfToken;
    }

    static ApiSession login(TestRestTemplate restTemplate, int port, String email, String password) {
        String baseUrl = "http://localhost:" + port;
        ResponseEntity<JsonNode> initialCsrf = restTemplate.getForEntity(baseUrl + "/api/v1/auth/csrf", JsonNode.class);
        assertThat(initialCsrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        String initialCsrfCookie = cookie(initialCsrf.getHeaders(), "XSRF-TOKEN");

        HttpHeaders loginHeaders = jsonHeaders();
        loginHeaders.set(HttpHeaders.COOKIE, initialCsrfCookie);
        loginHeaders.set("X-XSRF-TOKEN", cookieValue(initialCsrfCookie));
        ResponseEntity<JsonNode> login = restTemplate.exchange(
                baseUrl + "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email, "password", password), loginHeaders),
                JsonNode.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sessionCookie = cookie(login.getHeaders(), "JSESSIONID");

        HttpHeaders csrfHeaders = new HttpHeaders();
        csrfHeaders.set(HttpHeaders.COOKIE, sessionCookie);
        ResponseEntity<JsonNode> refreshedCsrf = restTemplate.exchange(
                baseUrl + "/api/v1/auth/csrf",
                HttpMethod.GET,
                new HttpEntity<>(csrfHeaders),
                JsonNode.class);
        assertThat(refreshedCsrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        String refreshedCsrfCookie = cookie(refreshedCsrf.getHeaders(), "XSRF-TOKEN");

        return new ApiSession(
                restTemplate,
                baseUrl,
                sessionCookie + "; " + refreshedCsrfCookie,
                cookieValue(refreshedCsrfCookie));
    }

    ResponseEntity<JsonNode> send(HttpMethod method, String path, Object body) {
        HttpHeaders headers = jsonHeaders();
        headers.set(HttpHeaders.COOKIE, cookieHeader);
        headers.set("X-XSRF-TOKEN", csrfToken);
        return restTemplate.exchange(baseUrl + path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }

    JsonNode send(HttpMethod method, String path, Object body, HttpStatus expectedStatus) {
        ResponseEntity<JsonNode> response = send(method, path, body);
        assertThat(response.getStatusCode()).as("%s %s", method, path).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    JsonNode graphql(String query, Map<String, Object> variables) {
        return send(HttpMethod.POST, "/graphql", Map.of("query", query, "variables", variables), HttpStatus.OK);
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static String cookie(HttpHeaders headers, String name) {
        String prefix = name + "=";
        return headers.getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(value -> value.startsWith(prefix))
                .map(value -> value.substring(0, value.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing cookie: " + name));
    }

    private static String cookieValue(String cookie) {
        int separator = cookie.indexOf('=');
        if (separator < 0 || separator == cookie.length() - 1) {
            throw new IllegalArgumentException("Invalid cookie");
        }
        return cookie.substring(separator + 1);
    }
}
