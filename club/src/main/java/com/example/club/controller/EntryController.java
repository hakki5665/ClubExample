package com.example.club.controller;

import com.example.club.dto.EntryRequestDto;
import com.example.club.dto.EntryResponseDto;
import com.example.club.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}")
@RequiredArgsConstructor
public class EntryController {
    private final ClubService clubService;

    @PostMapping("/entry")
    public ResponseEntity<EntryResponseDto> entry(@RequestBody @Valid EntryRequestDto request) {
        EntryResponseDto response = clubService.entryByQrCode(request.getQrCode());
        return ResponseEntity.ok(response);
    }
}
