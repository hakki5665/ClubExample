package com.example.club.repository;


import com.example.club.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
