package com.example.club.service;

import com.example.club.dto.EntryResponseDto;
import com.example.club.dto.ParticipantDto;
import com.example.club.dto.QrCodeDto;
import com.example.club.integration.AbstractIntegrationTest;
import com.example.club.repository.ParticipantRepository;
import com.example.club.repository.QrCodeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClubServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ClubService clubService;

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
    void testCreateParticipant() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");

        ParticipantDto result = clubService.createParticipant(dto);

        assertNotNull(result.getId());
        assertEquals("Иван Иванов", result.getFullName());
        assertNotNull(result.getQrCodes());
        assertEquals(1, result.getQrCodes().size());
        assertTrue(result.getQrCodes().get(0).getIsActive());
        assertNotNull(result.getQrCodes().get(0).getQrUuid());
    }

    @Test
    @Transactional
    void testGetAllParticipants() {
        ParticipantDto dto1 = new ParticipantDto();
        dto1.setFullName("Иван Иванов");
        clubService.createParticipant(dto1);

        ParticipantDto dto2 = new ParticipantDto();
        dto2.setFullName("Петр Петров");
        clubService.createParticipant(dto2);

        List<ParticipantDto> result = clubService.getAllParticipants();

        assertEquals(2, result.size());
    }

    @Test
    @Transactional
    void testGetParticipantById() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        ParticipantDto result = clubService.getParticipantById(created.getId());

        assertEquals("Иван Иванов", result.getFullName());
    }

    @Test
    void testGetParticipantByIdNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.getParticipantById(999L);
        });
        assertEquals("Участник не найден", exception.getMessage());
    }

    @Test
    void testUpdateParticipant() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        ParticipantDto update = new ParticipantDto();
        update.setFullName("Иван Петров");

        ParticipantDto result = clubService.updateParticipant(created.getId(), update);

        assertEquals("Иван Петров", result.getFullName());
    }

    @Test
    void testDeleteParticipant() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        clubService.deleteParticipant(created.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.getParticipantById(created.getId());
        });
        assertEquals("Участник не найден", exception.getMessage());
    }

    @Test
    void testEntryByQrCodeSuccess() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        UUID qrUuid = created.getQrCodes().get(0).getQrUuid();

        EntryResponseDto response = clubService.entryByQrCode(qrUuid);

        assertEquals("Иван Иванов", response.getFullName());
        assertEquals("Добро пожаловать в клуб", response.getMessage());
        assertNotNull(response.getNewQrCode());
        assertNotEquals(qrUuid, response.getNewQrCode());
    }

    @Test
    void testEntryByQrCodeNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.entryByQrCode(UUID.randomUUID());
        });
        assertEquals("QR-код не найден или уже использован", exception.getMessage());
    }

    @Test
    void testEntryByQrCodeAlreadyUsed() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        UUID qrUuid = created.getQrCodes().get(0).getQrUuid();

        clubService.entryByQrCode(qrUuid);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.entryByQrCode(qrUuid);
        });
        assertEquals("QR-код не найден или уже использован", exception.getMessage());
    }

    @Test
    void testCreateQrCodeSuccess() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        QrCodeDto result = clubService.createQrCode(created.getId());

        assertNotNull(result.getId());
        assertEquals(created.getId(), result.getParticipantId());
        assertTrue(result.getIsActive());
        assertNotNull(result.getQrUuid());
    }

    @Test
    void testCreateQrCodeForNotFoundParticipant() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.createQrCode(999L);
        });
        assertEquals("Участник не найден", exception.getMessage());
    }

    @Test
    void testGetAllQrCodes() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        clubService.createQrCode(created.getId());

        List<QrCodeDto> result = clubService.getAllQrCodes();

        assertEquals(2, result.size());
    }

    @Test
    void testDeleteQrCodeSuccess() {
        ParticipantDto dto = new ParticipantDto();
        dto.setFullName("Иван Иванов");
        ParticipantDto created = clubService.createParticipant(dto);

        QrCodeDto qrCode = clubService.createQrCode(created.getId());

        clubService.deleteQrCode(qrCode.getId());

        List<QrCodeDto> result = clubService.getAllQrCodes();
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteQrCodeNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.deleteQrCode(999L);
        });
        assertEquals("QR-код не найден или уже использован", exception.getMessage());
    }
}
