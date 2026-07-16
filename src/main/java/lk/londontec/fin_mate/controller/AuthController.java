package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.User;
import lk.londontec.fin_mate.repository.UserRepository;
import lk.londontec.fin_mate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .build();

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(user.getId(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(user.getId(), token));
    }

    public record RegisterRequest(String email, String password, String phoneNumber) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record AuthResponse(Long userId, String accessToken) {
    }
}
