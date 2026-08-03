package com.example.club.dto;

import java.util.UUID;

public class EntryResponseDto {
    private String fullName;
    private String message;
    private UUID newQrCode;

    public EntryResponseDto() {}

    public EntryResponseDto(String fullName, String message, UUID newQrCode) {
        this.fullName = fullName;
        this.message = message;
        this.newQrCode = newQrCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getNewQrCode() {
        return newQrCode;
    }

    public void setNewQrCode(UUID newQrCode) {
        this.newQrCode = newQrCode;
    }
}
