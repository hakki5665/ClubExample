package com.example.club.service;


import com.example.club.dto.EntryResponseDto;
import com.example.club.dto.ParticipantDto;
import com.example.club.dto.QrCodeDto;
import com.example.club.entity.Participant;
import com.example.club.entity.QrCode;
import com.example.club.repository.ParticipantRepository;
import com.example.club.repository.QrCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class ClubService {

    private final ParticipantRepository participantRepository;
    private final QrCodeRepository qrCodeRepository;

    @Transactional
    public EntryResponseDto entryByQrCode(UUID qrCodeUuid) {
        log.info("Попытка входа по QR-коду: {}", qrCodeUuid);


        QrCode qrCode = qrCodeRepository
                .findByUuidValueAndIsActiveTrue(qrCodeUuid)
                .orElseThrow(() -> new RuntimeException("QR-код не найден или уже использован"));

        Participant participant = qrCode.getParticipant();
        log.info("Найден участник: {}", participant.getFullName());

        qrCode.setIsActive(false);
        qrCodeRepository.save(qrCode);

        QrCode newQrCode = new QrCode();
        newQrCode.setUuidValue(UUID.randomUUID());
        newQrCode.setParticipant(participant);
        newQrCode.setIsActive(true);
        newQrCode.setCreatedAt(LocalDateTime.now());
        qrCodeRepository.save(newQrCode);

        return new EntryResponseDto(
                participant.getFullName(),
                "Добро пожаловать в клуб", newQrCode.getUuidValue()
        );
    }

    public List<ParticipantDto> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(this::convertToParticipantDto)
                .collect(Collectors.toList());
    }

    public ParticipantDto getParticipantById(Long id){
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));
        return convertToParticipantDto(participant);
    }
    @Transactional
    public ParticipantDto createParticipant(ParticipantDto participantDto) {
        Participant participant = new Participant();
        participant.setFullName(participantDto.getFullName());
        participant.setCreatedAt(LocalDateTime.now());

        QrCode qrCode = new QrCode();
        qrCode.setUuidValue(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());

        participant.getQrCodes().add(qrCode);

        Participant saved = participantRepository.save(participant);
        return convertToParticipantDto(saved);
    }
    @Transactional
    public ParticipantDto updateParticipant(Long id, ParticipantDto participantDto) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        participant.setFullName(participantDto.getFullName());
        Participant updated = participantRepository.save(participant);
        return convertToParticipantDto(updated);
    }

    @Transactional
    public void deleteParticipant(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new RuntimeException("Участник не найден");
        }
        participantRepository.deleteById(id);
    }

    public List<QrCodeDto> getAllQrCodes() {
        return qrCodeRepository.findAll().stream()
                .map(this::convertToQrCodeDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QrCodeDto createQrCode(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        QrCode qrCode = new QrCode();
        qrCode.setUuidValue(UUID.randomUUID());
        qrCode.setParticipant(participant);
        qrCode.setIsActive(true);
        qrCode.setCreatedAt(LocalDateTime.now());

        QrCode saved = qrCodeRepository.save(qrCode);
        return convertToQrCodeDto(saved);
    }

    @Transactional
    public void deleteQrCode(Long id) {
        if (!qrCodeRepository.existsById(id)) {
            throw new RuntimeException("QR-код не найден");
        }
        qrCodeRepository.deleteById(id);
    }

    private ParticipantDto convertToParticipantDto(Participant participant) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setFullName(participant.getFullName());
        dto.setCreatedAt(participant.getCreatedAt());
        if (participant.getQrCodes() !=null){
            dto.setQrCodes(participant.getQrCodes().stream()
                    .map(this::convertToQrCodeDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QrCodeDto convertToQrCodeDto(QrCode qrCode) {
        QrCodeDto dto = new QrCodeDto();
        dto.setId(qrCode.getId());
        dto.setUuidValue(qrCode.getUuidValue());
        dto.setParticipantId(qrCode.getParticipant().getId());
        dto.setIsActive(qrCode.getIsActive());
        dto.setCreatedAt(qrCode.getCreatedAt());
        return dto;
    }






}
