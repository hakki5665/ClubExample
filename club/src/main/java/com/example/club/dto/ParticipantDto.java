package com.example.club.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class ParticipantDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String fullName;
    private LocalDateTime createdAt;
    private List<QrCodeDto> qrCodes;

    public ParticipantDto() {}

    public ParticipantDto(Long id, String fullName, LocalDateTime createdAt, List<QrCodeDto> qrCodes) {
        this.id = id;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.qrCodes = qrCodes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QrCodeDto> getQrCodes() {
        return qrCodes;
    }

    public void setQrCodes(List<QrCodeDto> qrCodes) {
        this.qrCodes = qrCodes;
    }
}