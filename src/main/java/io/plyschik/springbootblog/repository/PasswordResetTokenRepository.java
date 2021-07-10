package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.VerificationToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository {
    Optional<VerificationToken> findByToken(String token);

    boolean existsByToken(String token);
}
