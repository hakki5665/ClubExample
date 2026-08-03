package com.example.club.controller;

import com.example.club.dto.ParticipantDto;
import com.example.club.integration.AbstractE2ETest;
import com.example.club.repository.ParticipantRepository;
import com.example.club.repository.QrCodeRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("E2E тесты ParticipantController")
class ParticipantControllerE2ETest extends AbstractE2ETest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @BeforeEach
    void cleanUp() {
        qrCodeRepository.deleteAll();
        participantRepository.deleteAll();
        wireMockServer.resetAll();  // <-- ДОБАВЛЕНО
    }


    @Test
    @DisplayName("Получение всех участников - успешный сценарий")
    void shouldGetAllParticipantsSuccessfully() {
    }

    @Test
    @DisplayName("Получение участника по ID - успешный сценарий")
    void shouldGetParticipantByIdSuccessfully() {
    }

    @Test
    @DisplayName("Получение участника по ID - не найден")
    void shouldReturnNotFound_WhenParticipantDoesNotExist() {

    }

    @Test
    @DisplayName("Создание участника - успешный сценарий")
    void shouldCreateParticipantSuccessfully() {

    }

    @Test
    @DisplayName("Создание участника - с пустым именем")
    void shouldReturnBadRequest_WhenEmptyName() {

    }

    @Test
    @DisplayName("Обновление участника - успешный сценарий")
    void shouldUpdateParticipantSuccessfully() {

    }

    @Test
    @DisplayName("Удаление участника - успешный сценарий")
    void shouldDeleteParticipantSuccessfully() {

    }

    @Test
    @DisplayName("Удаление несуществующего участника")
    void shouldReturnNotFound_WhenDeletingNonExistentParticipant() {

    }


    @Test
    @DisplayName("Создание участника - внешний сервис логирования НЕ вызывается")
    void shouldCreateParticipant_WithoutCallingExternalLogService() {

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

        // Создание участника
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Тест Тестов");

        HttpEntity<ParticipantDto> entity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                entity,
                ParticipantDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());

        // Проверка, что вызов внешнего сервиса НЕ производился
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }

    @Test
    @DisplayName("Обновление участника - внешний сервис логирования НЕ вызывается")
    void shouldUpdateParticipant_WithoutCallingExternalLogService() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

        // Создание участника
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );
        Long id = createResponse.getBody().getId();

        // Обновление
        ParticipantDto updateDto = new ParticipantDto();
        updateDto.setFullName("Иван Петров");
        HttpEntity<ParticipantDto> updateEntity = new HttpEntity<>(updateDto, getJsonHeaders());
        restTemplate.exchange(
                getBaseUrl() + "/participants/" + id,
                HttpMethod.PUT,
                updateEntity,
                ParticipantDto.class
        );

        // Проверка, что внешний сервис НЕ вызывался
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }

    @Test
    @DisplayName("Удаление участника - внешний сервис логирования НЕ вызывается")
    void shouldDeleteParticipant_WithoutCallingExternalLogService() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

        // Создание участника
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );
        Long id = createResponse.getBody().getId();

        // Удаление
        restTemplate.exchange(
                getBaseUrl() + "/participants/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Проверка, что внешний сервис НЕ вызывался
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }
}