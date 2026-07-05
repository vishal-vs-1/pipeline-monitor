package com.vigilant.vigilant_backend.controller;

import com.vigilant.vigilant_backend.dto.request.LoginRequest;
import com.vigilant.vigilant_backend.dto.request.RefreshTokenRequest;
import com.vigilant.vigilant_backend.dto.request.RegisterRequest;
import com.vigilant.vigilant_backend.dto.response.AuthResponse;
import com.vigilant.vigilant_backend.dto.response.UserDto;
import com.vigilant.vigilant_backend.entity.User;
import com.vigilant.vigilant_backend.repository.UserRepository;
import com.vigilant.vigilant_backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, UserDto.fromEntity(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProvider("local");
        
        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, UserDto.fromEntity(user)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (tokenProvider.validateToken(request.refreshToken())) {
            String email = tokenProvider.getEmailFromToken(request.refreshToken());
            User user = userRepository.findByEmail(email).orElseThrow();
            String accessToken = tokenProvider.generateAccessToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(accessToken, request.refreshToken(), UserDto.fromEntity(user)));
        } else {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}
