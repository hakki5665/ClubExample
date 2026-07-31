package com.example.club.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractE2ETest {

    @LocalServerPort
    protected int port;

    protected final RestTemplate restTemplate = new RestTemplate();

    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    protected static WireMockServer wireMockServer;
    protected static int wiremockPort;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true);
        POSTGRES_CONTAINER.start();

        wireMockServer = new WireMockServer(58090);
        wireMockServer.start();
        wiremockPort = wireMockServer.port();
        System.setProperty("wiremock.port", String.valueOf(wiremockPort));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.liquibase.enabled", () -> "false");

        registry.add("external.services.log.url", () -> "http://localhost:" + wiremockPort + "/api/external/log");
        registry.add("external.services.qr.url", () -> "http://localhost:" + wiremockPort + "/api/qr/generate");
    }

    @BeforeEach
    void resetWireMock() {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port + "/api/club";
    }

    protected HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected void stubExternalLogSuccess() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));
    }

    protected void stubExternalLogError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Service unavailable\"}")));
    }

    protected void stubExternalQrGenerateSuccess() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/qr/generate"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"qrCode\": \"base64image\"}")));
    }

    protected void stubExternalQrGenerateError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/qr/generate"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)));
    }
}