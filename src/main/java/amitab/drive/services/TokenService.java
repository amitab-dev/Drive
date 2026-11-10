package amitab.drive.services;

import amitab.drive.dto.TokenPair;
import amitab.drive.entity.Token;
import amitab.drive.entity.User;
import amitab.drive.repository.TokenRepository;
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
        t.setUserId(user);
        t.setExpiresAt(Instant.now().plusSeconds(refreshTokenTtl));
        t.setRefreshToken(hashedRefreshToken);

        return new TokenPair(accessToken, refreshToken);
    }
}
