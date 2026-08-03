package com.example.club.repository;


import com.example.club.entity.Participant;
import com.example.club.exception.ParticipantNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    default Participant findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(ParticipantNotFoundException::new);
    }
}
