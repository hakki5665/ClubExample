package com.example.club.service;

import com.example.club.dto.EntryResponseDto;
import com.example.club.dto.ParticipantDto;
import com.example.club.dto.QrCodeDto;
import com.example.club.entity.Participant;
import com.example.club.entity.QrCode;
import com.example.club.repository.ParticipantRepository;
import com.example.club.repository.QrCodeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClubService {
    private static final Logger log = LoggerFactory.getLogger(ClubService.class);
    private static final String WELCOME_MESSAGE = "Добро пожаловать в клуб";

    private final ParticipantRepository participantRepository;
    private final QrCodeRepository qrCodeRepository;
    private final RestTemplate restTemplate;

    @Value("${external.services.log.url:http://localhost:8081/api/external/log}")
    private String logServiceUrl;

    @Value("${external.services.qr.url:http://localhost:8081/api/qr/generate}")
    private String qrServiceUrl;

    public ClubService(ParticipantRepository participantRepository,
                       QrCodeRepository qrCodeRepository,
                       RestTemplate restTemplate) {
        this.participantRepository = participantRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public EntryResponseDto entryByQrCode(UUID qrCodeUuid) {
        log.info("Попытка входа по QR-коду: {}", qrCodeUuid);

        QrCode qrCode = qrCodeRepository.findByQrUuidAndIsActiveTrueOrThrow(qrCodeUuid);
        Participant participant = qrCode.getParticipant();
        log.info("Найден участник: {}", participant.getFullName());

        qrCode.setIsActive(false);
        qrCodeRepository.save(qrCode);

        QrCode newQrCode = new QrCode();
        newQrCode.setQrUuid(UUID.randomUUID());
        newQrCode.setParticipant(participant);
        newQrCode.setIsActive(true);
        newQrCode.setCreatedAt(LocalDateTime.now());
        qrCodeRepository.save(newQrCode);

        try {
            log.info("Вызов внешнего сервиса логирования по URL: {}", logServiceUrl);
            restTemplate.postForEntity(
                    logServiceUrl,
                    Map.of(
                            "participantName", participant.getFullName(),
                            "participantId", participant.getId(),
                            "qrCode", qrCodeUuid.toString(),
                            "newQrCode", newQrCode.getQrUuid().toString()
                    ),
                    Void.class
            );
            log.info("Внешний сервис логирования вызван успешно");
        } catch (Exception e) {
            log.warn("Ошибка при вызове внешнего сервиса логирования: {}", e.getMessage());
        }

        return new EntryResponseDto(
                participant.getFullName(),
                WELCOME_MESSAGE,
                newQrCode.getQrUuid()
        );
    }

    public List<ParticipantDto> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(this::convertToParticipantDto)
                .collect(Collectors.toList());
    }

    public ParticipantDto getParticipantById(Long id) {
        Participant participant = participantRepository.findByIdOrThrow(id);
        return convertToParticipantDto(participant);
    }

    @Transactional
    public ParticipantDto createParticipant(ParticipantDto participantDto) {
        Participant participant = new Participant();
        participant.setFullName(participantDto.getFullName());
        participant.setCreatedAt(LocalDateTime.now());

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());

        participant.getQrCodes().add(qrCode);

        Participant saved = participantRepository.save(participant);
        return convertToParticipantDto(saved);
    }

    @Transactional
    public ParticipantDto updateParticipant(Long id, ParticipantDto participantDto) {
        Participant participant = participantRepository.findByIdOrThrow(id);
        participant.setFullName(participantDto.getFullName());
        Participant updated = participantRepository.save(participant);
        return convertToParticipantDto(updated);
    }

    @Transactional
    public void deleteParticipant(Long id) {
        participantRepository.findByIdOrThrow(id);
        participantRepository.deleteById(id);
    }

    public List<QrCodeDto> getAllQrCodes() {
        return qrCodeRepository.findAll().stream()
                .map(this::convertToQrCodeDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QrCodeDto createQrCode(Long participantId) {
        Participant participant = participantRepository.findByIdOrThrow(participantId);

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());

        try {
            log.info("Вызов внешнего сервиса генерации QR по URL: {}", qrServiceUrl);
            restTemplate.postForEntity(
                    qrServiceUrl,
                    Map.of(
                            "participantId", participantId,
                            "participantName", participant.getFullName(),
                            "qrUuid", qrCode.getQrUuid().toString()
                    ),
                    Void.class
            );
            log.info("Внешний сервис генерации QR вызван успешно");
        } catch (Exception e) {
            log.warn("Ошибка при вызове внешнего сервиса генерации QR: {}", e.getMessage());
        }

        QrCode saved = qrCodeRepository.save(qrCode);
        return convertToQrCodeDto(saved);
    }

    @Transactional
    public void deleteQrCode(Long id) {
        qrCodeRepository.findByIdOrThrow(id);
        qrCodeRepository.deleteById(id);
    }

    private ParticipantDto convertToParticipantDto(Participant participant) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setFullName(participant.getFullName());
        dto.setCreatedAt(participant.getCreatedAt());
        if (participant.getQrCodes() != null) {
            dto.setQrCodes(participant.getQrCodes().stream()
                    .map(this::convertToQrCodeDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QrCodeDto convertToQrCodeDto(QrCode qrCode) {
        QrCodeDto dto = new QrCodeDto();
        dto.setId(qrCode.getId());
        dto.setQrUuid(qrCode.getQrUuid());
        dto.setParticipantId(qrCode.getParticipant().getId());
        dto.setIsActive(qrCode.getIsActive());
        dto.setCreatedAt(qrCode.getCreatedAt());
        return dto;
    }
}