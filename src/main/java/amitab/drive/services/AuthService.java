package amitab.drive.services;

import amitab.drive.dto.LoginRequestDTO;
import amitab.drive.dto.RegisterRequestDTO;
import amitab.drive.dto.RegisterResponseDTO;
import amitab.drive.dto.TokenPair;
import amitab.drive.entity.User;
import amitab.drive.exception.BadRequestException;
import amitab.drive.exception.ConflictException;
import amitab.drive.exception.NotFoundException;
import amitab.drive.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponseDTO registerUser(@Valid RegisterRequestDTO body) {
        String email = User.normalizeEmail(body.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with the email exists");
        }
        User u = new User();
        u.setFullName(body.fullName());
        u.setEmail(User.normalizeEmail(body.email()));
        u.setPassword(passwordEncoder.encode(body.password()));

        User createdUser = userRepository.save(u);
        return RegisterResponseDTO.from(createdUser);
    }

    @Transactional
    public TokenPair loginUser(LoginRequestDTO body) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email(), body.password())
        );
        User user = (User) authentication.getPrincipal();
        return tokenService.issueToken(user);
    }
}
