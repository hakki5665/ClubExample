package com.example.club.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class QrCodeDto {
    private Long id;
    private UUID qrUuid;
    private Long participantId;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public QrCodeDto() {}

    public QrCodeDto(Long id, UUID qrUuid, Long participantId, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.qrUuid = qrUuid;
        this.participantId = participantId;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getQrUuid() {
        return qrUuid;
    }

    public void setQrUuid(UUID qrUuid) {
        this.qrUuid = qrUuid;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}