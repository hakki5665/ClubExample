package com.example.club.controller;

import com.example.club.dto.EntryRequestDto;
import com.example.club.dto.EntryResponseDto;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E тесты EntryController")
class EntryControllerE2ETest extends AbstractE2ETest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @BeforeEach
    void cleanUp() {
        qrCodeRepository.deleteAll();
        participantRepository.deleteAll();
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Вход по QR-коду - успешный сценарий")
    void shouldEntrySuccessfully() {
        // Создание участника через API
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");

        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        UUID qrUuid = createResponse.getBody().getQrCodes().get(0).getQrUuid();

        // Выполнение входа
        EntryRequestDto request = new EntryRequestDto(qrUuid);
        HttpEntity<EntryRequestDto> entity = new HttpEntity<>(request, getJsonHeaders());

        ResponseEntity<EntryResponseDto> response = restTemplate.exchange(
                getBaseUrl() + "/entry",
                HttpMethod.POST,
                entity,
                EntryResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Иван Иванов", response.getBody().getFullName());
        assertEquals("Добро пожаловать в клуб", response.getBody().getMessage());
        assertNotNull(response.getBody().getNewQrCode());
        assertNotEquals(qrUuid, response.getBody().getNewQrCode());

        // Проверка, что старый QR стал неактивным
        ResponseEntity<ParticipantDto> getParticipantResponse = restTemplate.exchange(
                getBaseUrl() + "/participants/" + createResponse.getBody().getId(),
                HttpMethod.GET,
                null,
                ParticipantDto.class
        );

        assertNotNull(getParticipantResponse.getBody());
        assertEquals(2, getParticipantResponse.getBody().getQrCodes().size());

        boolean hasInactiveQr = getParticipantResponse.getBody().getQrCodes().stream()
                .anyMatch(qr -> !qr.getIsActive() && qr.getQrUuid().equals(qrUuid));
        assertTrue(hasInactiveQr);
    }

    @Test
    @DisplayName("Вход по QR-коду - QR-код не найден")
    void shouldReturnNotFound_WhenQrCodeDoesNotExist() {
        EntryRequestDto request = new EntryRequestDto(UUID.randomUUID());
        HttpEntity<EntryRequestDto> entity = new HttpEntity<>(request, getJsonHeaders());

        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/entry",
                        HttpMethod.POST,
                        entity,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Вход по QR-коду - QR-код уже использован")
    void shouldReturnNotFound_WhenQrCodeAlreadyUsed() {
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

        UUID qrUuid = createResponse.getBody().getQrCodes().get(0).getQrUuid();

        // Первый вход – успешный
        EntryRequestDto firstRequest = new EntryRequestDto(qrUuid);
        HttpEntity<EntryRequestDto> firstEntity = new HttpEntity<>(firstRequest, getJsonHeaders());
        restTemplate.exchange(getBaseUrl() + "/entry", HttpMethod.POST, firstEntity, EntryResponseDto.class);

        // Повторный вход – должен упасть с 404
        EntryRequestDto secondRequest = new EntryRequestDto(qrUuid);
        HttpEntity<EntryRequestDto> secondEntity = new HttpEntity<>(secondRequest, getJsonHeaders());

        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/entry",
                        HttpMethod.POST,
                        secondEntity,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Вход по QR-коду - с моком внешнего сервиса")
    void shouldEntrySuccessfully_WithExternalServiceMock() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"logged\"}")));

        // Создание участника
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Петр Петров");

        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        UUID qrUuid = createResponse.getBody().getQrCodes().get(0).getQrUuid();

        // Выполнение входа
        EntryRequestDto request = new EntryRequestDto(qrUuid);
        HttpEntity<EntryRequestDto> entity = new HttpEntity<>(request, getJsonHeaders());

        ResponseEntity<EntryResponseDto> response = restTemplate.exchange(
                getBaseUrl() + "/entry",
                HttpMethod.POST,
                entity,
                EntryResponseDto.class
        );

        // Проверка, что внешний сервис был вызван
        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/external/log"))
                .withRequestBody(WireMock.containing("Петр Петров")));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getNewQrCode());
    }

    @Test
    @DisplayName("Вход по QR-коду - внешний сервис вернул ошибку")
    void shouldHandleExternalServiceError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/external/log"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Service unavailable\"}")));

        // Создание участника
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Сергей Сергеев");

        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        UUID qrUuid = createResponse.getBody().getQrCodes().get(0).getQrUuid();

        // Выполнение вход – должен быть успешным, несмотря на ошибку внешнего сервиса
        EntryRequestDto request = new EntryRequestDto(qrUuid);
        HttpEntity<EntryRequestDto> entity = new HttpEntity<>(request, getJsonHeaders());

        ResponseEntity<EntryResponseDto> response = restTemplate.exchange(
                getBaseUrl() + "/entry",
                HttpMethod.POST,
                entity,
                EntryResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getNewQrCode());
    }
}