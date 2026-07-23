package com.example.club.controller;


import com.example.club.dto.EntryRequestDto;
import com.example.club.dto.EntryResponseDto;
import com.example.club.dto.ParticipantDto;
import com.example.club.dto.QrCodeDto;
import com.example.club.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club")
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;
    @PostMapping("/entry")
    public ResponseEntity<EntryResponseDto> entry(@RequestBody @Valid EntryRequestDto request) {
        EntryResponseDto response = clubService.entryByQrCode(request.getQrCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantDto>> getAllParticipants() {
        return ResponseEntity.ok(clubService.getAllParticipants());
    }

    @GetMapping("/participants/{id}")
    public ResponseEntity<ParticipantDto> getParticipant(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getParticipantById(id));
    }

    @PostMapping("/participants")
    public ResponseEntity<ParticipantDto> createParticipant(@RequestBody @Valid ParticipantDto participantDto) {
       ParticipantDto created = clubService.createParticipant(participantDto);
       return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/participants/{id}")
    public ResponseEntity<ParticipantDto> updateParticipants (
            @PathVariable Long id,
            @RequestBody @Valid ParticipantDto participantDto) {
        return ResponseEntity.ok(clubService.updateParticipant(id, participantDto));
    }

    @DeleteMapping("/participants/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        clubService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/qrcodes")
    public ResponseEntity<List<QrCodeDto>> getAllQrCodes() {
        return ResponseEntity.ok(clubService.getAllQrCodes());
    }

    @PostMapping("/qrcodes/{participantId}")
    public ResponseEntity<QrCodeDto> createQrCode(@PathVariable Long participantId) {
        QrCodeDto created = clubService.createQrCode(participantId);
        return  ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/qrcodes/{id}")
    public ResponseEntity<Void> deleteQrCode(@PathVariable Long id) {
        clubService.deleteQrCode(id);
        return  ResponseEntity.noContent().build();
    }

}
