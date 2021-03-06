package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.PasswordResetToken;
import io.plyschik.springbootblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    PasswordResetToken getByToken(String token);

    boolean existsByToken(String token);
    
    boolean existsByUser(User user);
}
