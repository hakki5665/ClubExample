package com.example.club.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class EntryResponseDto {
    private String fullName;
    private String message;
    private UUID newQrCode;
}
