package com.example.club.dto;

import java.util.UUID;

public class EntryRequestDto {
    private UUID qrCode;

    public EntryRequestDto() {}

    public EntryRequestDto(UUID qrCode) {
        this.qrCode = qrCode;
    }

    public UUID getQrCode() {
        return qrCode;
    }

    public void setQrCode(UUID qrCode) {
        this.qrCode = qrCode;
    }
}