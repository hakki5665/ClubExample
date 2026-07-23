package com.example.club.repository;


import com.example.club.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
     Optional<QrCode> findByUuidValueAndIsActiveTrue(UUID uuidValue);

     boolean existsByUuidValueAndIsActiveTrue(UUID uuidValue);
}
