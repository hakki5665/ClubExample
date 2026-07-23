package com.example.club.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class QrCodeDto {
    private Long id;
    private UUID uuidValue;
    private Long participantId;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
