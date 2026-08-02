package com.example.club.controller;

import com.example.club.dto.ParticipantDto;
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
    }

    @Test
    @DisplayName("Получение всех участников - успешный сценарий")
    void shouldGetAllParticipantsSuccessfully() {
        // 1. Создание двух участников
        ParticipantDto participant1 = new ParticipantDto();
        participant1.setFullName("Иван Иванов");
        HttpEntity<ParticipantDto> entity1 = new HttpEntity<>(participant1, getJsonHeaders());
        restTemplate.exchange(getBaseUrl() + "/participants", HttpMethod.POST, entity1, ParticipantDto.class);

        ParticipantDto participant2 = new ParticipantDto();
        participant2.setFullName("Петр Петров");
        HttpEntity<ParticipantDto> entity2 = new HttpEntity<>(participant2, getJsonHeaders());
        restTemplate.exchange(getBaseUrl() + "/participants", HttpMethod.POST, entity2, ParticipantDto.class);

        // 2. Получение списка участников
        ResponseEntity<ParticipantDto[]> response = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.GET,
                null,
                ParticipantDto[].class
        );

        // 3. Проверка ответа
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    @DisplayName("Получение участника по ID - успешный сценарий")
    void shouldGetParticipantByIdSuccessfully() {
        // 1. Создание участника
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

        // 2. Получение участника по ID
        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.GET,
                null,
                ParticipantDto.class
        );

        // 3. Проверка ответа
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
        // 1. Подготавка данных
        ParticipantDto participantDto = new ParticipantDto();
        participantDto.setFullName("Иван Иванов");

        // 2. Создание участника
        HttpEntity<ParticipantDto> entity = new HttpEntity<>(participantDto, getJsonHeaders());
        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants",
                HttpMethod.POST,
                entity,
                ParticipantDto.class
        );

        // 3. Проверка ответа
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Иван Иванов", response.getBody().getFullName());
        assertNotNull(response.getBody().getQrCodes());
        assertEquals(1, response.getBody().getQrCodes().size());
        assertTrue(response.getBody().getQrCodes().get(0).getIsActive());
        assertNotNull(response.getBody().getQrCodes().get(0).getQrUuid());

        // 4. Проверка, что участник действительно создан в БД
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
        // 1. Создание участника
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

        // 2. Обновление участника
        ParticipantDto updateDto = new ParticipantDto();
        updateDto.setFullName("Иван Петров");

        HttpEntity<ParticipantDto> updateEntity = new HttpEntity<>(updateDto, getJsonHeaders());

        ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.PUT,
                updateEntity,
                ParticipantDto.class
        );

        // 3. Проверка ответа
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Иван Петров", response.getBody().getFullName());

        // 4. Проверка, что обновление сохранилось
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
        // 1. Создание участника
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

        // 2. Удаление участника
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/participants/" + participantId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // 3. Проверка ответа
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // 4. Проверка, что участник действительно удален
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
}