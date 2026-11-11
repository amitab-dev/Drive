package amitab.drive.repository;

import amitab.drive.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByRefreshToken(String hashedRefreshToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Token t
                    SET t.revokedAt = :now, t.updatedAt = :now
                    WHERE t.user.id = :userId AND t.revokedAt IS NULL
            """)
    void revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
