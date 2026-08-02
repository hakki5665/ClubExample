package com.example.club.repository;

import com.example.club.entity.Participant;
import com.example.club.entity.QrCode;
import com.example.club.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeRepositoryTest extends AbstractIntegrationTest {

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
    void testSaveQrCode() {
        Participant participant = createParticipant("Иван Иванов");

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());

        QrCode saved = qrCodeRepository.save(qrCode);

        assertNotNull(saved.getId());
        assertEquals(participant.getId(), saved.getParticipant().getId());
        assertTrue(saved.getIsActive());
    }

    @Test
    void testFindByQrUuidAndIsActiveTrue() {
        Participant participant = createParticipant("Иван Иванов");
        UUID uuid = UUID.randomUUID();

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(uuid);
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());
        qrCodeRepository.save(qrCode);

        var found = qrCodeRepository.findByQrUuidAndIsActiveTrue(uuid);

        assertTrue(found.isPresent());
        assertEquals(uuid, found.get().getQrUuid());
        assertTrue(found.get().getIsActive());
    }

    @Test
    void testFindByQrUuidAndIsActiveTrueNotFound() {
        var found = qrCodeRepository.findByQrUuidAndIsActiveTrue(UUID.randomUUID());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdOrThrow() {
        Participant participant = createParticipant("Иван Иванов");
        QrCode qrCode = createQrCode(participant);
        QrCode saved = qrCodeRepository.save(qrCode);

        QrCode found = qrCodeRepository.findByIdOrThrow(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void testFindByIdOrThrowNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            qrCodeRepository.findByIdOrThrow(999L);
        });
        assertEquals("QR-код не найден или уже использован", exception.getMessage());
    }

    @Test
    void testFindByQrUuidAndIsActiveTrueOrThrow() {
        Participant participant = createParticipant("Иван Иванов");
        UUID uuid = UUID.randomUUID();

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(uuid);
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());
        qrCodeRepository.save(qrCode);

        QrCode found = qrCodeRepository.findByQrUuidAndIsActiveTrueOrThrow(uuid);

        assertNotNull(found);
        assertEquals(uuid, found.getQrUuid());
    }

    @Test
    void testFindByQrUuidAndIsActiveTrueOrThrowNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            qrCodeRepository.findByQrUuidAndIsActiveTrueOrThrow(UUID.randomUUID());
        });
        assertEquals("QR-код не найден или уже использован", exception.getMessage());
    }

    private Participant createParticipant(String name) {
        Participant participant = new Participant();
        participant.setFullName(name);
        participant.setCreatedAt(LocalDateTime.now());
        return participantRepository.save(participant);
    }

    private QrCode createQrCode(Participant participant) {
        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());
        return qrCode;
    }
}