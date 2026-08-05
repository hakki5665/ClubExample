package com.example.club.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QrCode> qrCodes = new ArrayList<>();

    public Participant() {}

    public Participant(Long id, String fullName, LocalDateTime createdAt, List<QrCode> qrCodes) {
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

    public List<QrCode> getQrCodes() {
        return qrCodes;
    }

    public void setQrCodes(List<QrCode> qrCodes) {
        this.qrCodes = qrCodes;
    }
}