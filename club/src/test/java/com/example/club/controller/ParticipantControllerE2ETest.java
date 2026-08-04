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
import org.springframework.web.client.HttpClientErrorException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

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
        WIREMOCK.resetAll();
    }

    @Test
    @DisplayName("Получение всех участников - успешный сценарий")
    void shouldGetAllParticipantsSuccessfully() {
        ParticipantDto participant1 = new ParticipantDto();
        participant1.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> entity1 = new HttpEntity<>(participant1, getJsonHeaders());
        restTemplate.exchange(getBaseUrl() + "/participants", HttpMethod.POST, entity1, ParticipantDto.class);

        ParticipantDto participant2 = new ParticipantDto();
        participant2.setFullName("Петр Петров");
        HttpEntity<ParticipantDto> entity2 = new HttpEntity<>(participant2, getJsonHeaders());
        restTemplate.exchange(getBaseUrl() + "/participants", HttpMethod.POST, entity2, ParticipantDto.class);

        ResponseEntity<ParticipantDto[]> response = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.GET,
                null,
                ParticipantDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    @DisplayName("Получение участника по ID - успешный сценарий")
    void shouldGetParticipantByIdSuccessfully() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        Long participantId = createResponse.getBody().getId();

        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.GET,
                null,
                ParticipantDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Иван Иванов", response.getBody().getFullName());
        assertNotNull(response.getBody().getQrCodes());
        assertEquals(1, response.getBody().getQrCodes().size());
        assertTrue(response.getBody().getQrCodes().get(0).getIsActive());
    }

    @Test
    @DisplayName("Получение участника по ID - не найден")
    void shouldReturnNotFound_WhenParticipantDoesNotExist() {
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/participants/999",
                        HttpMethod.GET,
                        null,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Создание участника - успешный сценарий")
    void shouldCreateParticipantSuccessfully() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");

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
        assertEquals("Иван Иванов", response.getBody().getFullName());
        assertNotNull(response.getBody().getQrCodes());
        assertEquals(1, response.getBody().getQrCodes().size());
        assertTrue(response.getBody().getQrCodes().get(0).getIsActive());
        assertNotNull(response.getBody().getQrCodes().get(0).getQrUuid());

        ResponseEntity<ParticipantDto> getResponse = restTemplate.exchange(
                getBaseUrl() + "/participants/" + response.getBody().getId(),
                HttpMethod.GET,
                null,
                ParticipantDto.class
        );
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Создание участника - с пустым именем")
    void shouldReturnBadRequest_WhenEmptyName() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("");

        HttpEntity<ParticipantDto> entity = new HttpEntity<>(participantDto, getJsonHeaders());

        HttpClientErrorException.BadRequest exception = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/participants",
                        HttpMethod.POST,
                        entity,
                        String.class
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("Обновление участника - успешный сценарий")
    void shouldUpdateParticipantSuccessfully() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        Long participantId = createResponse.getBody().getId();

        ParticipantDto updateDto = new ParticipantDto();
        updateDto.setFullName("Иван Петров");
        HttpEntity<ParticipantDto> updateEntity = new HttpEntity<>(updateDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.PUT,
                updateEntity,
                ParticipantDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Иван Петров", response.getBody().getFullName());

        ResponseEntity<ParticipantDto> getResponse = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.GET,
                null,
                ParticipantDto.class
        );
        assertEquals("Иван Петров", getResponse.getBody().getFullName());
    }

    @Test
    @DisplayName("Удаление участника - успешный сценарий")
    void shouldDeleteParticipantSuccessfully() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        Long participantId = createResponse.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/participants/" + participantId,
                        HttpMethod.GET,
                        null,
                        String.class
                )
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Удаление несуществующего участника")
    void shouldReturnNotFound_WhenDeletingNonExistentParticipant() {
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/participants/999",
                        HttpMethod.DELETE,
                        null,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Создание участника - внешний сервис логирования НЕ вызывается")
    void shouldCreateParticipant_WithoutCallingExternalLogService() {
        WIREMOCK.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

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

        WIREMOCK.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }

    @Test
    @DisplayName("Обновление участника - внешний сервис логирования НЕ вызывается")
    void shouldUpdateParticipant_WithoutCallingExternalLogService() {
        WIREMOCK.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

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

        ParticipantDto updateDto = new ParticipantDto();
        updateDto.setFullName("Иван Петров");
        HttpEntity<ParticipantDto> updateEntity = new HttpEntity<>(updateDto, getJsonHeaders());
        restTemplate.exchange(
                getBaseUrl() + "/participants/" + id,
                HttpMethod.PUT,
                updateEntity,
                ParticipantDto.class
        );

        WIREMOCK.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }

    @Test
    @DisplayName("Удаление участника - внешний сервис логирования НЕ вызывается")
    void shouldDeleteParticipant_WithoutCallingExternalLogService() {
        WIREMOCK.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

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

        restTemplate.exchange(
                getBaseUrl() + "/participants/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        WIREMOCK.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log")));
    }
}
