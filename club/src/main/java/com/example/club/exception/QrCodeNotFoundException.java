package com.example.club.exception;

public class QrCodeNotFoundException extends ClubException {

    public static final String MESSAGE = "QR-код не найден или уже использован";

    public QrCodeNotFoundException() {
        super(MESSAGE);
    }
}