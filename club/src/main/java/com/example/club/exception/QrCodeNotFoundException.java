package com.example.club.exception;

public class QrCodeNotFoundException extends ClubException {
    public QrCodeNotFoundException() {
        super("QR-код не найден или уже использован");
    }
}
