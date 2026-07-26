package com.example.club.repository;


import com.example.club.entity.QrCode;
import com.example.club.exception.QrCodeNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    Optional<QrCode> findByUuidValueAndIsActiveTrue(UUID uuidValue);

    boolean existsByUuidValueAndIsActiveTrue(UUID uuidValue);

    default QrCode findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(QrCodeNotFoundException::new);
    }

    default QrCode findByUuidValueAndIsActiveTrueOrThrow(UUID uuidValue) {
        return findByUuidValueAndIsActiveTrue(uuidValue)
                .orElseThrow(QrCodeNotFoundException::new);
    }
}
