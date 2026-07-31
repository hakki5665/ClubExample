package com.example.club.exception;

public class ParticipantNotFoundException extends ClubException {
    public ParticipantNotFoundException() {
        super("Участник не найден");
    }
}
