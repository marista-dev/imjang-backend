package com.imjang.domain.auth.repository;

import com.imjang.domain.auth.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

  Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

  Optional<EmailVerification> findByEmailAndCode(String email, String code);

}
