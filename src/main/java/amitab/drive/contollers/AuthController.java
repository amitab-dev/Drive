package amitab.drive.contollers;

import amitab.drive.dto.*;
import amitab.drive.entity.User;
import amitab.drive.exception.UnauthorizedException;
import amitab.drive.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final static String ACCESS_TOKEN_NAME = "drive_access_token";
    private final static String REFRESH_TOKEN_NAME = "drive_refresh_token";
    private final AuthService authService;

    @PostMapping("/")
    public ResponseEntity<RegisterResponseDTO> registerUser(
            @RequestBody RegisterRequestDTO body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(body));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(
            @RequestBody LoginRequestDTO body,
            HttpServletResponse res
    ) {
        TokenPair tokens = authService.loginUser(body);
        setTokenCookies(res, tokens);
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponseDTO(tokens.accessToken()));
    }

    @GetMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logoutUser(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logoutUser(refreshToken);
        }
        clearTokenCookies(response);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("User logged out"));
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token not found, please login");
        }
        TokenPair tokens = authService.refreshAccessToken(refreshToken);
        setTokenCookies(response, tokens);
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponseDTO(tokens.accessToken()));
    }

    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie ck : cookies) {
            if (REFRESH_TOKEN_NAME.equals(ck.getName())) {
                return ck.getValue();
            }
        }
        return null;
    }

    private void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookies = setCookies(ACCESS_TOKEN_NAME, "", 0);
        ResponseCookie refreshCookies = setCookies(REFRESH_TOKEN_NAME, "", 0);
        response.addHeader("Set-Cookie", accessCookies.toString());
        response.addHeader("Set-Cookie", refreshCookies.toString());
    }

    private void setTokenCookies(HttpServletResponse res, TokenPair tokens) {
        ResponseCookie accessCookie = setCookies(ACCESS_TOKEN_NAME, tokens.accessToken(), 15);
        ResponseCookie refreshCookie = setCookies(REFRESH_TOKEN_NAME, tokens.refreshToken(), 43200);
        res.addHeader("Set-Cookie", accessCookie.toString());
        res.addHeader("Set-Cookie", refreshCookie.toString());
    }

    private ResponseCookie setCookies(String name, String value, long durationInMin) {
        return ResponseCookie
                .from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("lax")
                .maxAge(Duration.ofMinutes(durationInMin))
                .build();
    }
}
