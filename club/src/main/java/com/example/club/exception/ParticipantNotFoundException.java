package com.example.club.exception;

public class ParticipantNotFoundException extends ClubException {

    public static final String MESSAGE = "Участник не найден";

    public ParticipantNotFoundException() {
        super(MESSAGE);
    }
}