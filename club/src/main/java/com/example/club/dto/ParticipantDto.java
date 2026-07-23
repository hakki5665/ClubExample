package com.example.club.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ParticipantDto {
    private Long id;
    private String fullName;
    private LocalDateTime createdAt;
    private List<QrCodeDto> qrCodes;
}
