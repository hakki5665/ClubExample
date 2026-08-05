package com.example.club.controller;

import com.example.club.dto.QrCodeDto;
import com.example.club.service.ClubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/qrcodes")
public class QrCodeController {

    private final ClubService clubService;

    public QrCodeController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<List<QrCodeDto>> getAllQrCodes() {
        return ResponseEntity.ok(clubService.getAllQrCodes());
    }

    @PostMapping("/{participantId}")
    public ResponseEntity<QrCodeDto> createQrCode(@PathVariable Long participantId) {
        QrCodeDto created = clubService.createQrCode(participantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(@PathVariable Long id) {
        clubService.deleteQrCode(id);
        return ResponseEntity.noContent().build();
    }
}