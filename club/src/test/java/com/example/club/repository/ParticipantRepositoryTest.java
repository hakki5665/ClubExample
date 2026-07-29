package com.example.club.repository;

import com.example.club.entity.Participant;
import com.example.club.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void cleanUp() {
        participantRepository.deleteAll();
    }

    @Test
    void testSaveParticipant() {
        Participant participant = new Participant();
        participant.setFullName("Иван Иванов");
        participant.setCreatedAt(LocalDateTime.now());

        Participant saved = participantRepository.save(participant);

        assertNotNull(saved.getId());
        assertEquals("Иван Иванов", saved.getFullName());
    }

    @Test
    void testFindByIdOrThrow() {
        Participant participant = new Participant();
        participant.setFullName("Иван Иванов");
        participant.setCreatedAt(LocalDateTime.now());
        Participant saved = participantRepository.save(participant);

        Participant found = participantRepository.findByIdOrThrow(saved.getId());

        assertNotNull(found);
        assertEquals("Иван Иванов", found.getFullName());
    }

    @Test
    void testFindByIdOrThrowNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            participantRepository.findByIdOrThrow(999L);
        });
        assertEquals("Участник не найден", exception.getMessage());
    }
}