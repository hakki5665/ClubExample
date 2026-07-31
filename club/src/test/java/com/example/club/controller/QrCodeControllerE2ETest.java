package com.example.club.controller;

import com.example.club.dto.ParticipantDto;
import com.example.club.dto.QrCodeDto;
import com.example.club.integration.AbstractE2ETest;
import com.example.club.repository.ParticipantRepository;
import com.example.club.repository.QrCodeRepository;
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

@DisplayName("E2E тесты QrCodeController")
class QrCodeControllerE2ETest extends AbstractE2ETest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @BeforeEach
    void cleanUp() {
        qrCodeRepository.deleteAll();
        participantRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение всех QR-кодов - успешный сценарий")
    void shouldGetAllQrCodesSuccessfully() {
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

        HttpEntity<Void> qrEntity = new HttpEntity<>(null, getJsonHeaders());
        restTemplate.exchange(
                getBaseUrl() + "/qrcodes/" + participantId,
                HttpMethod.POST,
                qrEntity,
                QrCodeDto.class
        );

        ResponseEntity<QrCodeDto[]> response = restTemplate.exchange(
                getBaseUrl() + "/qrcodes",
                HttpMethod.GET,
                null,
                QrCodeDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    @DisplayName("Создание QR-кода - успешный сценарий")
    void shouldCreateQrCodeSuccessfully() {
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

        HttpEntity<Void> qrEntity = new HttpEntity<>(null, getJsonHeaders());
        ResponseEntity<QrCodeDto> response = restTemplate.exchange(
                getBaseUrl() + "/qrcodes/" + participantId,
                HttpMethod.POST,
                qrEntity,
                QrCodeDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getQrUuid());
        assertEquals(participantId, response.getBody().getParticipantId());
        assertTrue(response.getBody().getIsActive());
    }

    @Test
    @DisplayName("Создание QR-кода - участник не найден")
    void shouldReturnNotFound_WhenParticipantDoesNotExist() {
        HttpEntity<Void> qrEntity = new HttpEntity<>(null, getJsonHeaders());

        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/qrcodes/999",
                        HttpMethod.POST,
                        qrEntity,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Удаление QR-кода - успешный сценарий")
    void shouldDeleteQrCodeSuccessfully() {
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");

        HttpEntity<ParticipantDto> createEntity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> createResponse = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                createEntity,
                ParticipantDto.class
        );

        Long qrCodeId = createResponse.getBody().getQrCodes().get(0).getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/qrcodes/" + qrCodeId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<QrCodeDto[]> getAllResponse = restTemplate.exchange(
                getBaseUrl() + "/qrcodes",
                HttpMethod.GET,
                null,
                QrCodeDto[].class
        );
        assertEquals(0, getAllResponse.getBody().length);
    }

    @Test
    @DisplayName("Удаление QR-кода - QR-код не найден")
    void shouldReturnNotFound_WhenQrCodeDoesNotExist() {
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(
                        getBaseUrl() + "/qrcodes/999",
                        HttpMethod.DELETE,
                        null,
                        String.class
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Создание QR-кода с моком внешнего сервиса")
    void shouldCreateQrCode_WithExternalServiceMock() {
        stubExternalQrGenerateSuccess();

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

        HttpEntity<Void> qrEntity = new HttpEntity<>(null, getJsonHeaders());
        ResponseEntity<QrCodeDto> response = restTemplate.exchange(
                getBaseUrl() + "/qrcodes/" + participantId,
                HttpMethod.POST,
                qrEntity,
                QrCodeDto.class
        );

        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/qr/generate")));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getQrUuid());
    }
}