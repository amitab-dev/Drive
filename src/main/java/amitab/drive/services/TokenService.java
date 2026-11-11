package amitab.drive.services;

import amitab.drive.dto.TokenPair;
import amitab.drive.entity.Token;
import amitab.drive.entity.User;
import amitab.drive.exception.NotFoundException;
import amitab.drive.exception.UnauthorizedException;
import amitab.drive.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenRepository tokenRepository;

    @Value("${jwt.refresh-ttl-in-sec}")
    private long refreshTokenTtl;

    public TokenPair issueToken(User user) {
        String accessToken = jwtService.issueAccessToken(user);
        String refreshToken = refreshTokenService.issueRefreshToken();
        String hashedRefreshToken = refreshTokenService.hashRefreshToken(refreshToken);

        Token t = new Token();
        t.setUser(user);
        t.setExpiresAt(Instant.now().plusSeconds(refreshTokenTtl));
        t.setRefreshToken(hashedRefreshToken);

        tokenRepository.save(t);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        String hashedRefreshToken = refreshTokenService.hashRefreshToken(refreshToken);
        tokenRepository.findByRefreshToken(hashedRefreshToken)
                .filter((token) -> token.getRevokedAt() == null)
                .ifPresent((token) -> token.setRevokedAt(Instant.now()));
    }

    @Transactional(dontRollbackOn = UnauthorizedException.class)
    public TokenPair rotateRefreshToken(String refreshToken) {
        String hashedRefreshToken = refreshTokenService.hashRefreshToken(refreshToken);
        Instant now = Instant.now();
        Token token = tokenRepository.findByRefreshToken(hashedRefreshToken)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        User user = token.getUser();
        if (token.getRevokedAt() != null) {
            tokenRepository.revokeAllForUser(user.getId(), now);
            throw new UnauthorizedException("Refresh token reuse detected");
        }
        if (token.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token is expired");
        }
        token.setRevokedAt(now);
        return issueToken(user);
    }
}
