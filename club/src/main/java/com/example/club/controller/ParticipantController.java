package com.example.club.controller;

import com.example.club.dto.ParticipantDto;
import com.example.club.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/participants")
@RequiredArgsConstructor
public class ParticipantController {
    private final ClubService clubService;

    @GetMapping
    public ResponseEntity<List<ParticipantDto>> getAllParticipants() {
        return ResponseEntity.ok(clubService.getAllParticipants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDto> getParticipant(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getParticipantById(id));
    }

    @PostMapping
    public ResponseEntity<ParticipantDto> createParticipant(@RequestBody @Valid ParticipantDto participantDto) {
        ParticipantDto created = clubService.createParticipant(participantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipantDto> updateParticipant(
            @PathVariable Long id,
            @RequestBody @Valid ParticipantDto participantDto) {
        return ResponseEntity.ok(clubService.updateParticipant(id, participantDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        clubService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }
}